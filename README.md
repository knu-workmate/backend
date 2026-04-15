## 📄 API 문서 (Swagger)

* Swagger UI: `http://localhost:8080/swagger-ui/index.html`

<!-- JWT 관련 api 시작-->


<details>
<summary> <h2>JWT</h2> </summary>

---

## 🔐 인증 처리 방식

* JWT는 **Spring Security 필터에서 자동 처리됨**
* 토큰이 없거나 유효하지 않으면 자동으로 401/403 응답

---

## 👤 사용자 정보 사용

`CurrentUser`를 주입받아 필요한 정보만 꺼내서 사용합니다.
(com.workmate.workmate.global.security.CurrnetUser)

### 제공 메서드

* `getUserId()` : 현재 로그인한 사용자 ID 반환
* `getUserRole()` : 현재 사용자 권한(Role) 반환

### 사용 예시 (Service)

```java
@Service
public class SomeService {

    private final CurrentUser currentUser;

    // 생성자 주입
    public SomeService(CurrentUser currentUser) {
        this.currentUser = currentUser;
    }

    public void someLogic() {
        Long userId = currentUser.getUserId();
        String role = currentUser.getUserRole();

        /** 구현 **/
    }
}
```


---

## ✅ 요약

- 인증은 필터에서 자동 처리
- 사용자 정보는 `CurrentUser`에서 필요한 값만 꺼내서 사용

</details>




<details>

<!-- 게시판 api 시작-->

<summary><h2> 게시판 </h2></summary>

<br>

### POST: 게시판 생성

Endpoint: `/boards`

새로운 게시판을 생성합니다. 

-  Request Body 

| params        | 타입                      |  설명   |
|---------------|-------------------------|-------|
| 'boardName'   | varchar                 | 유저 아이디 |
| 'type'        | enum('NORMAL, 'NOTICE') | 비밀번호  |
| 'workPlaceId' | bigint                  | 이름    |

- Response Body
```json

{
  "boardId": 1,
  "boardName": "자유게시판",
  "type": "NORMAL"
}

```
---




</details>

