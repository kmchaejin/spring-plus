# Spring Plus - AWS 인프라 구축 및 연동
## Health Check API
- **URL**: `http://<탄력적IP>:8080/health`
- **Method**: `GET`
- **Description**: 서버 접속 및 Live 상태를 확인할 수 있는 API입니다.
- **인증 필요**: 없음
- **응답 예시**:

![image](https://github.com/user-attachments/assets/579243ae-96cc-4500-9dc2-a76a0fa55555)


<br>
<br>

## EC2 인스턴스에 RDS 연결
- EC2에서 실행되는 애플리케이션이 RDS(MySQL)에 연결되어 동작합니다. <br>
- 보안 그룹 설정을 통해 EC2 → RDS 접근을 허용했습니다.

![image](https://github.com/user-attachments/assets/45870dc3-4ff8-4093-b405-e2c677ab6c3c)

<br>
<br>

## S3 이미지 업로드
- 사용자의 프로필 이미지를 S3에 업로드하고 관리할 수 있는 기능을 구현했습니다. <br>
- 버킷의 퍼블릭 접근 정책 및 객체 접근 권한 설정을 적용했습니다.

![image](https://github.com/user-attachments/assets/2b52c11c-4ba3-4d63-9939-538bb2fa9324)
