## 동시성 제어 테스트

동시성 : 하나의 메서드에 여러 스레드가 접근하여 진행하는 것

#### 문제점
* 문제점 : 값이 하나씩 바뀌어야하는데, 동시적으로 값을 가져가서 수정해버릴 수 있다.
    * ex 1 : 호텔 예약 시, 방이 1개씩 예약되어 빠져나가야 한다.
      <br>
      방이 1개 남은 그 때, 2명의 고객이 동시적으로 예약을 진행하면 남은방이 -1이 될 수 있다.
    * ex 2 : 나에게 남은 사과가 2개 일 때, 3명이 동시에 사과를 요청하여 가져간다면
      <br>
      남은 사과가 -1개가 될 수 있다.

#### 해결 방법
* 해결 방법
    * 방법 1 : syncronized 사용
        ```
            @Transactional
            public synchronized boolean reserve_with_synchronized(Long hotelId, Person person){
                Hotel hotel = hotelRepository.findById(hotelId).orElseThrow();
                if(hotel.getRestCount() > 0){
                    hotelRepository.minusRestCount(hotelId);
                }
                return true;
            }
        ```     
      * 메서드 위에 @Transactional을 사용하면, 데이터베이스의 동일한 Entity의 접근에 대해서만 동시에 수정되는 것을 방지한다.
        * why? transaction의 begin과 commit 부분은 syncronized 의 일부가 아니기 때문에 begin을 동시에 한다면 같은 값을 여러 스레드에서 가져가 수정하고 commit 순서에 따라 값이 덮어지게된다.
      * 메서드 위에 @Transactional을 사용하지 않는다면, 현재 local pc에서는 동시성 문제가 해결됨.
        <br>
        but! ) **자바의 syncronized는 하나의 프로세스에서만 보장**되기 때문에, 서버가 여러대라면 동시성 문제가 다시 발행한다.
<br><br>
  * 방법 2 : syncronized 사용 + @Transactional의 isolation속성(Serializable)을 이용
     ```
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public synchronized void minusRestCount(Long id, Long quantity){
     ```
    * Serializable : 특정 트랜잭션이 사용중인 테이블의 모든 행을 다른 트랜잭션이 접근할 수 없도록 잠근다.
    <br>가장 높은 데이터 정합성을 갖으나, 성능은 가장 떨어진다. 이 격리 수준에서는 단순한 SELECT 쿼리가 실행되더라도, 데이터베이스 락이 걸려 다른 트랜잭션에서 데이터에 접근할 수 없게된다.
<br><br>
  * 방법 3 : 비관적 락 사용
    ```    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT h FROM Hotel h WHERE h.id = :hotelId")
    Optional<Hotel> findByIdLock(@Param("hotelId") Long hotelId);
    ```
    * LockModeType.PESSIMISTIC_WRITE<br>
      일반적인 옵션. 데이터베이스에 쓰기 락<br>
      다른 트랜잭션에서 읽기도 쓰기도 못함. (배타적 잠금)
    * LockModeType.PESSIMISTIC_READ<br>
    반복 읽기만하고 수정하지 않는 용도로 락을 걸 때 사용<br>
    다른 트랜잭션에서 읽기는 가능함. (공유 잠금)
    * LockModeType.PESSINISTIC_FORCE_INCREMENT<br>
    Version 정보를 사용하는 비관적 락
<br><br>      
  * 방법 4 : 낙관적 락 사용<br>
    ```
            try {
                hotelOptimisticService.reserve_with_optimistic_lock(hotel.getId(), p_2);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
    ```
    * try-catch 처리를 진행해야 rollback되지 않음.
    * 특이한 특성 : 먼저 접근하여 수정한 사람이 아닌, **먼저 커밋한 사람**에게 우선권을 줌

=> 방법 3와 방법 4인 낙관적 락(Optimistic Lock)과 비관적 락(Pessimistic Lock)은 싱글 DB 환경인 경우에만 적용 가능한 개념이다.

* 방법 5 : 분산 DB 환경에서 분산 락(Distributed Lock) 활용
    * **분산 락(Distributed Lock)** : 경쟁 상황(race condition)에서 하나의 공유자원에 접근할 때, 데이터의 결함이 발생하지 않도록 원자성(atomic)을 보장하는 기법


syncronized + @Transactional(isolation) 출처 : https://ssseung.tistory.com/538 <br/>
비관적, 낙관적 락 이론 출처 : https://isntyet.github.io/jpa/JPA-%EB%B9%84%EA%B4%80%EC%A0%81-%EC%9E%A0%EA%B8%88(Pessimistic-Lock)/ <br/>



---
### JPA lock 과 @Transactional-isolation
1. 비관적 잠금 : lock이 해제 될 때 까지 다른 트랜잭션이 리소스에 접근하거나 수정하지 못하도록 잠궈버리는 것
```LockModeType.PESSIMISTIC_WRITE```
2. 낙관점 잠금 : 여러 트랜잭션이 동시에 리소스에 액세스하고 수정할 수 있도록 허용하지만 업데이트 시 충돌이 감지되는 전략으로, 이는 일반적으로 버전 번호나 타임스탬프를 사용하여 수행됩니다.
```
@Entity
public class Hotel_Optimistic {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;
}
```
3. @Transactional 격리수준(isolation) : 트랜잭션 내의 작업이 다른 트랜잭션에 표시되는 정도를 결정합니다. 격리 수준이 높을수록 격리 수준이 높아지지만 성능이 저하될 수 있습니다.
```
@Transactional(isolation = Isolation.READ_COMMITTED)
public void myTransactionalMethod() {
    // Transactional code
}
```

### 예시와 함께 알아보는 JPA lock 과 @Transactional-isolation
대전제 : 도서관에 있는 책은 여러 사람이 빌릴 수 있다.
<br>또한, 사람들이 공유 문서로 공동 작업하는 공유 작업 공간이 있다.
* **JPA lock** : "나는 이 책을 빌리고 있는데, 내가 다 읽을 때까지 다른 사람이 그것을 만지는 것을 원하지 않습니다."
    * 비관적 잠금 : 책에 대한 특별 잠금을 요청합니다. 당신이 끝날 때까지 다른 누구도 그것을 가져갈 수 없습니다.
    * 낙관적 잠금(버전 관리) : 누구나 책을 빌릴 수 있지만, 반납할 때 도서관에서는 그 사이에 다른 사람이 수정했는지 확인합니다. 버전이 같다면 충돌이 일어나지 않음.
* **@Transactional - isolation** : 한 사람이 함께 작업하는 동안 한 사람의 편집 내용이 다른 사람에게 얼마나 영향을 미치는지 결정하는 것과 같습니다. 
    * READ COMMITTED: 전체 문서 편집을 마친 후에만 다른 사람들이 변경 사항을 볼 수 있습니다. 
    * REPEATABLE READ: 다른 사람들은 귀하가 작업을 완료할 때까지 귀하가 편집 중인 부분을 볼 수 있을 뿐만 아니라 변경할 수도 없습니다.
    * SERIALIZABLE: 공유 작업 공간에서 문서를 일시적으로 떼어내는 것과 같습니다. 완료될 때까지 누구도 건드릴 수 없습니다.

간단히 말해서, JPA의 잠금은 도서관 도서 예약과 같이 데이터베이스의 특정 엔터티에 대한 액세스를 제어하는 것입니다. @Transactional의 격리는 공유 작업 공간의 공동 작업자가 문서를 보고 수정하는 방법을 결정하는 것과 같이 트랜잭션이 함께 작동하는 방법에 대한 규칙을 설정하는 것입니다.


### 차이점
1. 세분성 
    - 비관적 및 낙관적 잠금은 엔터티 또는 레코드 수준에서 작동하여 데이터베이스의 특정 레코드에 대한 액세스를 제어합니다.
        * ex) 엔터티(예: 도서관의 책)를 잠그는 것 => 데이터베이스의 개별 엔터티 또는 레코드 수준
    - 트랜잭션 격리 수준은 전체 트랜잭션 수준에서 작동하여 한 트랜잭션에서 다른 트랜잭션에 대한 변경 사항의 조회 등을 제어합니다.
        * ex) 조회 및 변경 측면에서 트랜잭션(예: 공유 문서에서 공동 작업하는 사람들)이 작동하는 방식 => 트랜잭션의 시작부터 끝까지의 전체 트랜잭션 수준에서 작동
2. 잠금 메커니즘
    - 비관적 잠금은 명시적 잠금을 사용하여 동일한 리소스에 대한 동시 액세스를 방지합니다.
    - 낙관적 잠금은 업데이트 시 충돌을 감지하기 위해 버전 관리 또는 타임스탬프에 의존합니다.
    - 트랜잭션 격리 수준은 트랜잭션이 서로의 변경 사항을 확인하는 방법을 제어합니다.
3. 동시성 제어 전략
    - 비관적 잠금은 리소스에 대한 동시 액세스를 방지하는 전략입니다.
    - 낙관적 잠금은 업데이트 시 충돌 감지를 기반으로 하는 전략입니다. => 버전 충돌로 알 수 있음.
    - 트랜잭션 격리 수준은 변경 사항의 조회 측면에서 트랜잭션이 서로 상호 작용하는 방식을 정의합니다.

세 가지 메커니즘은 모두 데이터베이스의 동시성 제어와 관련되어 있지만 목표를 달성하기 위해 서로 다른 수준에서 작동하고 서로 다른 전략을 사용합니다.
비관적 및 낙관적 잠금은 특정 기록에 대한 액세스 제어에 중점을 두는 반면, 트랜잭션 격리 수준은 변경 사항의 가시성을 제어하는데 중점을 둡니다.


