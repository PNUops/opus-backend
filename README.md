<p align="center">
  <img width="700" height="432" alt="Image" src="https://github.com/user-attachments/assets/3c21ae9b-834b-41b0-b103-29e5e7f81754" />
</p>

<h1 align="center">🎓 OPUS (SW프로젝트관리시스템)</h1>

<p align="center">
<strong>OPUS</strong>는 부산대학교 내 SW 프로젝트(캡스톤/해커톤/교과 등)의 성과를 <strong>등록, 관리, 공유</strong>하고,<br>
운영 측면에서는 <strong>대회/팀/결과물</strong>을 효율적으로 관리하는 시스템입니다.
</p>


<p align="center">
  <a href="https://opus.pusan.ac.kr/">🌐 운영 서비스 이동</a> | 
  <a href="https://github.com/PNUops/opus-backend">🧩 Production Repo 이동</a> | 
  <a href="https://opus.pnu.app/api/docs/">📄 Spring Rest Docs </a> | 
  <a href="https://opus.pnu.app">🔨 개발 서버 </a> | 
  <a href="https://github.com/PNUops/ops-mvp-back">📦 MVP Backend</a>
</p>
<br>

<h2 align="left">🛠 Tech Stack</h2>

<p align="center">
  <strong>Language & Core</strong><br>
<br>
  <img src="https://img.shields.io/badge/-Jdk%2017-437291?style=for-the-badge&logo=openjdk&logoColor=white">
  <img src="https://img.shields.io/badge/-Java-8D6748?style=for-the-badge&logo=java&logoColor=white">
  <img src="https://img.shields.io/badge/Spring%20boot%203.5.7-6DB33F?style=for-the-badge&logo=springboot&logoColor=white">
  <img src="https://img.shields.io/badge/Spring%20Security-6DB33F?style=for-the-badge&logo=Spring-Security&logoColor=white">
</p>

<p align="center">
  <strong>Data & Media</strong><br>
<br>
  <img src="https://img.shields.io/badge/Spring%20data%20jpa-6DB33F?style=for-the-badge&logo=springboot&logoColor=white">
  <img src="https://img.shields.io/badge/MySQL%208.0-005C84?style=for-the-badge&logo=mysql&logoColor=white">
  <img src="https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=Redis&logoColor=white">
  <img src="https://img.shields.io/badge/Scrimage(WebP)-FF6F00?style=for-the-badge&logo=image&logoColor=white">
</p>

<p align="center">
  <strong>Documentation & Test</strong><br>
<br>
  <img src="https://img.shields.io/badge/Spring%20rest%20docs-6DB33F?style=for-the-badge&logo=springboot&logoColor=white">
  <img src="https://img.shields.io/badge/Asciidoctor-E40046?style=for-the-badge&logo=asciidoctor&logoColor=white">
  <img src="https://img.shields.io/badge/JUnit5-25A162?style=for-the-badge&logo=junit5&logoColor=white">
  <img src="https://img.shields.io/badge/-Mockito-6DB33F?style=for-the-badge">
</p>
<br>

## 🏗️ Structure
```
─ src
   ├─ main
   │  ├─ java
   │  │  └─ com
   │  │     └─ opus
   │  │        └─ opus
   │  │           ├─ OpusApplication.java
   │  │           ├─ docs
   │  │           │  └─ asciidoc
   │  │           ├─ global
   │  │           │  ├─ base
   │  │           │  ├─ config
   │  │           │  ├─ error
   │  │           │  └─ security
   │  │           └─ modules
   │  │              ├─ member                         
   │  │              │  ├─ api
   │  │              │  │  └─ Membercontroller.java
   │  │              │  ├─ application
   │  │              │  │  ├─ convenience
   │  │              │  │     └─ MemberConvenience.java
   │  │              │  │  └─ dto
   │  │              │  │     ├─ request
   │  │              │  │     └─ response
   │  │              │  │  ├─ MemberCommandService.java
   │  │              │  │  └─ MemberQueryService.java
   │  │              │  ├─ domain
   │  │              │  │  ├─ Member.java
   │  │              │  │  ├─ dao
   │  │              │  │     └─ MemberRepository.java
   │  │              │  └─ exception
   │  │              │     ├─ MemberException.java
   │  │              │     └─ MemberExceptionType.java
   │  │              ├─ contest
   │  │              ├─ file
   │  │              ├─ notice
   │  │              └─ team
   │  └─ resources
   │     ├─ application.yml
   │     ├─ application-secret.yml
   │     └─ schema.sql
   └─ test
      ├─ java
      │  └─ com
      │     └─ opus
      │        └─ opus
      │           ├─ helper
      │           │ ├─ ApiTestHelper.java
      │           │  └─ IntegrationTest.java
      │           ├─ restdocs
      │           │  ├─ docs
      │           │      ├─ MemberApiDocsTest.java
      │           │      └─ ...
      │           │  ├─ RestDocsConfig.java
      │           │  └─ RestDocsTest.java  
      │           ├─ member                         
      │           │  ├─ application
      │           │      ├─ MemberCommandServiceTestjava
      │           │      └─ MemberQueryServiceTest.java
      │           │  └─ MemberFixture
      │           ├─ ...     
      └─ resources
```
<br>

## ✨ Key Features & Engineering Points
(추가 예정)
<br>

## 🗂 Database ERD
(추가 예정)
<br>

## 🌐 Infra Structure
 <img width="383" height="228" alt="Image" src="https://github.com/user-attachments/assets/cafec3d7-eeda-4f58-a3c7-344f46ed4481" />
 <br>

## 👥 Backend Member

<div align="left">
  <table>
  <tr>
    <td align="center">
      이지민
    </td>    
    <td align="center">
      김태윤
    </td>
    <td align="center">
      문여원
    </td>
    <td align="center">
      문성재
    </td>
  </tr>
  <tr>
    <td align="center">
      <a href="https://github.com/JJimini">
        <img src="https://github.com/JJimini.png" width="80" alt="JJimini"/>
        <br/>
        <sub><b>JJimini</b></sub>
      </a>
      <br/>
    </td>
    <td align="center">
      <a href="https://github.com/pykido">
      <img src="https://github.com/pykido.png" width="80" alt="pykido"/>
      <br />
      <sub><b>pykido</b></sub>
      </a>
      <br/>
    </td>
    <td align="center">
      <a href="https://github.com/myeowon">
      <img src="https://github.com/myeowon.png" width="80" alt="myeowon"/>
      <br />
      <sub><b>myeowon</b></sub>
      </a>
      <br/>
    </td>
    <td align="center">
      <a href="https://github.com/sjmoon00">
      <img src="https://github.com/sjmoon00.png" width="80" alt="sjmoon00"/>
      <br />
      <sub><b>sjmoon00</b></sub>
      </a>
      <br/>
    </td>
  </tr>
</table>
</div>
<br>

## 🤝Project Contributors
#### Backend / Infra / Frontend /Design
[![contributors](https://contrib.rocks/image?repo=PNUops/opus-backend)](https://github.com/PNUops/opus-backend/graphs/contributors)
[![contributors](https://contrib.rocks/image?repo=PNUops/ops-mvp-front)](https://github.com/PNUops/ops-mvp-front/graphs/contributors)
