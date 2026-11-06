# Sổ khám thú cưng — Pet Health Passport

Ứng dụng Android giúp lưu hồ sơ sức khỏe thú cưng: lịch tiêm phòng/tẩy giun, bệnh án,
biểu đồ cân nặng, nhắc nhở, mã QR hồ sơ, cẩm nang chăm sóc, trắc nghiệm kiến thức,
danh bạ phòng khám và cộng đồng chia sẻ. Giao diện song ngữ Việt / Anh.

## Tính năng chính

- **Nhiều thú cưng**: chó, mèo, chim, thỏ và loài khác; ảnh, giống, ngày sinh, cân nặng,
  microchip, chủ nuôi, ghi chú.
- **Tiêm phòng & tẩy giun**: tự động tính ngày nhắc lại (vaccine +12 tháng, tẩy giun +3 tháng),
  trạng thái quá hạn / sắp tới / đã lên lịch, bật tắt nhắc riêng từng mũi.
- **Bệnh án**: ngày khám, phòng khám, bác sĩ, chẩn đoán, đơn thuốc, ảnh toa thuốc.
- **Cân nặng**: biểu đồ MPAndroidChart, thống kê cân gần nhất, khoảng cân, mức thay đổi.
- **Nhắc nhở**: WorkManager chạy hằng ngày theo giờ người dùng chọn, thông báo nhóm theo
  từng thú cưng, mở thẳng hồ sơ khi nhấn thông báo.
- **QR hồ sơ**: tạo mã QR bằng ZXing, chia sẻ văn bản; quét QR để mở hồ sơ trên máy.
- **Cẩm nang**: 12 bài viết về dinh dưỡng, bệnh học và chăm sóc; tìm kiếm và lọc theo chủ đề.
- **Trắc nghiệm**: 7 câu hỏi, phản hồi đúng/sai kèm giải thích, tổng kết điểm.
- **Phòng khám**: gọi điện bằng `ACTION_DIAL`, mở bản đồ bằng `geo:` (không cần API key).
- **Cộng đồng**: đăng bài, lọc theo loài, thích, bình luận, xoá bài; hoạt động ngoại tuyến,
  tự đồng bộ Firestore khi có cấu hình Firebase.
- **Cài đặt**: đổi ngôn ngữ Việt/Anh, tên hiển thị, bật/tắt nhắc nhở, giờ nhắc, gửi thông báo thử,
  đặt lại dữ liệu demo. Lần chạy đầu luôn mặc định tiếng Việt, sau đó ghi nhớ lựa chọn của người dùng.
- **Dữ liệu mẫu**: Milo (Golden Retriever) và Luna (British Shorthair) cùng lịch tiêm, bệnh án,
  cân nặng và bài viết cộng đồng được tạo ở lần chạy đầu.

## Công nghệ

| Thành phần | Lựa chọn |
| --- | --- |
| Ngôn ngữ | Kotlin 2.0.21 |
| UI | XML + ViewBinding, Material 3 |
| Kiến trúc | MVVM, Repository, StateFlow |
| Lưu trữ | Room 2.6.1 (nguồn dữ liệu chính, offline-first) |
| Nền | WorkManager 2.9.1 |
| Biểu đồ | MPAndroidChart 3.1.0 |
| QR | ZXing core + journeyapps zxing-android-embedded |
| Ảnh | Coil |
| Cộng đồng (tuỳ chọn) | Firebase Firestore (BoM 33.7.0) |
| Build | AGP 8.7.3, Gradle 8.9, JDK 17, compileSdk 35, minSdk 24 |

## Yêu cầu môi trường

- Android Studio Ladybug trở lên (khuyến nghị) hoặc Android SDK command-line tools.
- JDK 17. Không dùng JDK 21/25 vì AGP 8.7 có thể lỗi.
- Android SDK Platform 35 và Build Tools tương ứng.

## Chạy ứng dụng

### Android Studio

1. Mở thư mục dự án.
2. Chờ Gradle sync.
3. Chọn thiết bị/emulator và bấm **Run**.

### Dòng lệnh (Windows)

```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.0.15.6-hotspot"
.\gradlew.bat assembleDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`

Cài lên thiết bị:

```powershell
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

### Lưu ý về đường dẫn có dấu tiếng Việt

Dự án đặt tại `D:\app thú cưng`. `gradle.properties` đã bật
`android.overridePathCheck=true` để AGP chấp nhận đường dẫn không phải ASCII.

Nếu Java/Gradle không mở được thư mục do encoding của Windows (lỗi
`ClassNotFoundException: org.gradle.wrapper.GradleWrapperMain` hoặc lỗi đường dẫn),
hãy mở dự án bằng Android Studio hoặc build qua một junction ASCII:

```powershell
cmd /c mklink /J "C:\petapp" "D:\app thú cưng"
cd C:\petapp
.\gradlew.bat assembleDebug
```

## Firebase (tuỳ chọn)

Ứng dụng chạy đầy đủ khi **không** có Firebase. Cộng đồng sẽ dùng dữ liệu cục bộ và
hiển thị trạng thái “Đang dùng dữ liệu ngoại tuyến”.

Để bật đồng bộ cộng đồng:

1. Tạo Firebase project, thêm ứng dụng Android với package `com.pethealth.passport`.
2. Tải `google-services.json` và đặt vào `app/google-services.json`.
3. Bật Cloud Firestore và tạo collection `posts`.
4. Build lại. Plugin `com.google.gms.google-services` chỉ được áp dụng khi file tồn tại.
5. Quy tắc Firestore mẫu cho phát triển:

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /posts/{post} {
      allow read: if true;
      allow create, update, delete: if true; // chỉ dùng khi thử nghiệm
    }
  }
}
```

Ảnh đính kèm bài viết chỉ lưu cục bộ, không tải lên Firebase.

## Quyền

- `POST_NOTIFICATIONS` (Android 13+): xin quyền khi mở app hoặc khi bật nhắc nhở/gửi thử.
- `CAMERA`: chỉ dùng khi quét mã QR.

## Cấu trúc chính

```
app/src/main/java/com/pethealth/passport/
├── data/
│   ├── local/          # Room entity, DAO, database
│   ├── model/          # Species, DueStatus
│   ├── repository/     # Pet, Health, Community, Content, Settings
│   └── seed/           # Dữ liệu demo + reset
├── di/                 # AppContainer
├── ui/
│   ├── home/           # Trang chủ
│   ├── pets/           # Danh sách, sửa, chi tiết + QR
│   ├── health/         # Tiêm phòng, bệnh án, cân nặng
│   ├── community/      # Feed, đăng bài, chi tiết bài
│   ├── content/        # Cẩm nang, trắc nghiệm, phòng khám
│   ├── settings/       # Cài đặt
│   └── more/           # Màn hình Thêm
├── util/               # Dates, QR, thông báo, lịch nhắc, ảnh
└── worker/             # ReminderWorker
```

Chuỗi giao diện: `res/values/strings.xml` (tiếng Việt, mặc định) và
`res/values-en/strings.xml` (tiếng Anh).

## Ghi chú phát triển

- Room là nguồn dữ liệu chính; Firebase chỉ đồng bộ bài viết cộng đồng.
- Lịch nhắc dùng `PeriodicWorkRequest` duy nhất tên `pet_reminders_daily`, được cập nhật
  khi đổi giờ hoặc bật/tắt nhắc nhở.
- Ngày “nhắc lại” được tính tự động khi thêm mũi tiêm nhưng vẫn có thể sửa tay.
- Nhấn “Đặt lại dữ liệu demo” trong Cài đặt để xoá toàn bộ dữ liệu cục bộ và tạo lại mẫu.
