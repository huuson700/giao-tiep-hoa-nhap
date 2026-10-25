# Giao Tiếp Hòa Nhập 2.0

Ứng dụng Android hỗ trợ giao tiếp bằng câu có sẵn, văn bản và giọng nói.

## Tính năng
- Giao diện tiếng Việt, các thẻ câu dễ chạm và tương phản cao.
- Thư viện câu theo nhóm: nhu cầu cơ bản, sức khỏe, giúp đỡ, cảm xúc, giao tiếp, sinh hoạt, ăn uống và khẩn cấp.
- Đọc câu bằng Text-to-Speech tiếng Việt (phụ thuộc giọng đọc có sẵn trên thiết bị).
- Nhận diện giọng nói tiếng Việt qua dịch vụ speech recognition trên Android.
- Lưu câu yêu thích cục bộ trên thiết bị.
- Cài đặt tự đọc, cỡ chữ và tốc độ đọc.
- Chia sẻ lời nhắn sang ứng dụng tương thích qua bảng chia sẻ của Android.

## Xây dựng APK
GitHub Actions xây dựng APK debug khi có push lên main hoặc nhánh feature, hoặc chạy thủ công trong tab Actions.

Artifact: `Giao-Tiep-Hoa-Nhap-debug-APK`

## Yêu cầu
- JDK 17
- Android SDK Platform 35
- Gradle 8.9
- Android Gradle Plugin 8.7.3

## Lưu ý
Ứng dụng chưa có máy chủ nhắn tin riêng; nút chia sẻ mở bảng chia sẻ Android để người dùng chọn ứng dụng. Nhận diện giọng nói và giọng đọc có thể cần kết nối mạng hoặc dịch vụ tương ứng tùy thiết bị.
