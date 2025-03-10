# Board App

## MSA Architecture with

- Spring Boot
- JPA
- Docker
- MySQL
- Redis
- Kafka
- Gradle Multi Module

---

## Services

- article (게시글 관련 서비스)
- article-read (게시글 조회 최적화 관련 서비스)
- comment (댓글 관련 서비스)
- hot-article (인기글 관련 서비스)
- like (게시글 좋아요 관련 서비스)
- view (게시글 조회 관련 서비스)

---

## Architecture

- 게시글 서비스, 댓글 서비스, 좋아요 서비스, 조회수 서비스는 Producer
- 인기글 서비스, 게시글 조회 서비스는 Consumer
- 인기글 서비스는 배치 대신, 실시간으로 각 Producer 들로부터 이벤트를 받아 당일 인기글 서비스를 선정
- 게시글 조회 서비스는 게시글의 특징을 고려했을 때, 조회 트래픽이 쓰기 트래픽보다 압도적으로 많을 가능성이 있고 그에 따라,
CQRS(Command Query Responsibility Segregation) 개념을 활용하여 쓰기와 읽기 서비스를 분리