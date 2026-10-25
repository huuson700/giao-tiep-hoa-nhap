# Giao Tiếp Hòa Nhập 2.0

Ứng dụng Android hỗ trợ giao tiếp bằng câu có sẵn, văn bản, giọng nói và trò chuyện trực tuyến theo phòng.

## Tính năng
- Giao diện tiếng Việt, các thẻ câu dễ chạm và tương phản cao.
- Thư viện câu theo nhóm: nhu cầu cơ bản, sức khỏe, giúp đỡ, cảm xúc, giao tiếp, sinh hoạt, ăn uống và khẩn cấp.
- Đọc câu bằng Text-to-Speech tiếng Việt (phụ thuộc giọng đọc có sẵn trên thiết bị).
- Nhận diện giọng nói tiếng Việt qua dịch vụ speech recognition trên Android.
- Lưu câu yêu thích cục bộ trên thiết bị.
- Cài đặt tự đọc, cỡ chữ và tốc độ đọc.
- Chia sẻ lời nhắn sang ứng dụng tương thích qua bảng chia sẻ Android.
- Nhắn tin thời gian thực qua Firebase Authentication + Cloud Firestore: tài khoản email/mật khẩu, tạo phòng, mã mời 8 ký tự và tin nhắn đồng bộ trực tiếp.

## Cấu hình nhắn tin trực tuyến (bắt buộc)
Để bảo vệ người dùng, ứng dụng không chứa sẵn thông tin dự án Firebase của riêng ai. Quản trị viên cần tạo và cấu hình Firebase trước khi dùng tính năng chat:

1. Tạo project tại Firebase Console: https://console.firebase.google.com/
2. Trong Project settings → General, thêm Android app với package name `vn.hoanhap`.
3. Vào Authentication → Sign-in method và bật **Email/Password**.
4. Tạo Cloud Firestore Database.
5. Mở Rules của Firestore và dán nội dung tệp `firestore.rules` trong repository, sau đó Publish.
6. Trong Project settings → General → Your apps, lấy **Web API Key**, **App ID** và **Project ID**.
7. Mở ứng dụng → Tin nhắn trực tuyến → dán ba giá trị trên vào màn hình thiết lập Firebase.
8. Tạo tài khoản bằng email, đăng nhập, tạo phòng và chia sẻ mã mời riêng tư với người cần trò chuyện.

**Bảo mật và quyền riêng tư**
- Không tải lên, chia sẻ hoặc nhúng service-account key vào ứng dụng Android.
- Firebase API key không phải mật khẩu; bảo mật thực tế dựa vào Authentication và Firestore Security Rules.
- Mã mời cho phép người có mã tự tham gia phòng; chỉ chia sẻ với người tin tưởng.
- Bản đầu giới hạn 50 thành viên mỗi phòng và 1.000 ký tự mỗi tin nhắn.
- Tài khoản Firebase và dữ liệu Firestore thuộc project do người triển khai quản lý. Hãy cấu hình ngân sách/giám sát và chính sách lưu trữ trước khi phát hành rộng rãi.
- Chưa hỗ trợ mã hóa đầu-cuối (E2EE), gửi ảnh/tệp, thông báo đẩy hoặc quản trị/xóa tài khoản từ trong ứng dụng. Không dùng phiên bản này để gửi thông tin đặc biệt nhạy cảm.

## Xây dựng APK
GitHub Actions xây dựng APK debug khi có push lên main hoặc nhánh feature, hoặc chạy thủ công trong tab Actions.

Artifact: `Giao-Tiep-Hoa-Nhap-debug-APK`

## Yêu cầu
- JDK 17
- Android SDK Platform 35
- Gradle 8.9
- Android Gradle Plugin 8.7.3
