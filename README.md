# Kotlin Spring Batch

kotlin에서 spring batch 를 적용해보자

## 목차

- [Kotlin Spring Batch](#kotlin-spring-batch)
    - [목차](#목차)
    - [개념정리](#개념정리)
        - [Job & Step / Scope?](#job--step--scope)
        - [Reader](#reader)
        - [Writer](#writer)
        - [Processor](#processor)

## 개념정리

Spring Batch에서 가장 중요한 요소 3개는 바로

* Job
* Step
* Reader / Writer / Processor

3개가 존재합니다

### Job & Step / Scope?

Job은 Spring Batch 실행시에 Job Name을 지정해서 실행할 수 있으며

```text
--job.name=addressChangeJob
```

와 같이 런타임에 어떠한 Job을 실행할 것인지  
결정할 수 있습니다

Step 같은 경우에는 실제로 Job에서 어떠한 과정을 가지고 흐를 것인지   
결정하게 하는 요소인데요.

가장 중요한 요소는 바로

```java
@Scope(value = "job", proxyMode = ScopedProxyMode.TARGET_CLASS)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JobScope {

}
```

Step에 **JobParameter** 와 함께 대한 `Scope` 을 정의할 수 있다는 것인데요.  
일반적으로 `Step` 을 bean 으로 등록해서 싱글톤으로 활용하기 마련인데,  
이는 병렬 실행, 혹은 테스트 구동시에 같은 인스턴스로 실행되게 됩니다.  
그러면 상태 충돌 문제, 똑같은 Step 에 대한 동일성을 보장하지 못하게 되는데요  
그래서 `JobParameter`에 따라서 서로 다른 `Step` 인스턴스를 만들기 위해서  
`JobScope` 어노테이션을 활용하여 인스턴스를 새로 생성하게 됩니다

결과적으로,

1. JobParameter 의 지연 Binding이 가능하다
2. 같은 컴포넌트에 대해 병렬 실행할 수 있게 된다

로 정리할 수 있습니다.

> Spring Batch 는 같은 Parameter에 대해서 중복 실행이 불가능 합니다  
> Spring Batch Metadata 테이블이 따로 Parameter들을 관리해주기 때문이죠

### Reader

```java
/**
 *
 * ItemReader defines the batch artifact that reads 
 * items for chunk processing.
 *
 */
public interface ItemReader {
    void open(Serializable checkpoint) throws Exception;

    void close() throws Exception;

    Object readItem() throws Exception;

    Serializable checkpointInfo() throws Exception;
}
```

쉽게 이야기하면, 데이터를 읽어오기 위한 책임을 가지고 있는 모듈

크게 2가지 근본 인터페이스가 있는데요

1. ItemReader
2. ItemStream

`ItemStream` 이 `ItemReader`의 상태를 저장하는 역할을 하고 있습니다  
그래서 **ItemStream** 을 바탕으로 더 효과적인 Database query가 가능하게 된거죠

Reader의 종류에는 또 2가지로 분류됩니다

1. Cursor: stream을 계속 열어서 데이터를 1개씩 읽어오는 방식
2. Paging: Page 단위로 **ChunkSize** 만큼 읽어오는 방식

2방식의 가장 큰 차이는 `connection` 을 어떻게 관리하느냐의 차이인데,  
**Cursor** 방식의 경우에는 `connection` 을 계속 물고 있고,  
**Paging** 방식의 경우에는 `connection` 을 사용하고 반납하게 됩니다

> Paging의 경우에는 주의할 점이 하나 있는데요.  
> 매번마다 어떠한 기준에 의해서 Paging 처리를 할 것인지 결정해야 됩니다.  
> 그렇지 않으면 내부적으로 임의의 값으로 인해 Paging 하게 되는 것이죠  
> 보통은 일반적으로 ID 값을 기준으로 정렬하면 됩니다 (혹은 Cursor를 사용하거나)

역시나 `Jpa`를 많이 사용하는 추세이므로,  
**JpaCursorItemReader** 혹은 **JpaPagingItemReader** 를 활용해주시면 됩니다

### Writer

```java

/**
 *
 * ItemWriter defines the batch artifact that writes to a 
 * list of items for chunk processing.
 *
 */
public interface ItemWriter {
    void open(Serializable checkpoint) throws Exception;

    void close() throws Exception;

    void writeItems(List<Object> items) throws Exception;

    Serializable checkpointInfo() throws Exception;
}
```

데이터를 쓰기 위한 책임을 가지고 있는 모듈

일반적으로 `Jpa` 를 요즘 많이 사용하는 추세이므로,  
`JpaItemWriter`를 잘 활용해주시면 됩니다.  
`EntityManagerFactory` 만 잘 설정해주면, 알아서 **entityManager.merge()** 하게 됩니다

### Processor

데이터를 가공하기 위한 책임을 가지고 있는 모듈

```java

/**
 * ItemProcessor is used in chunk processing
 * to operate on an input item and produce
 * an output item.
 *
 */
public interface ItemProcessor {
    Object processItem(Object item) throws Exception;
}
```

인터페이스에서 명시되어 있듯이 `item` -> `anotherItem` 이 되는 구조 입니다  
데이터에 대한 변환이 이루어진다고 보면 되요 ㅎㅎ 너무나 간단하죠?  

