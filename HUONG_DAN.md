# HƯỚNG DẪN TẠO ỨNG DỤNG "SAVIFY KARAOKE" CHO ANDROID TV

Hướng dẫn này viết cho người không rành máy tính. Làm **từng bước một**, đọc kỹ, không cần vội.
Toàn bộ chỉ cần: **một chiếc máy tính có mạng** và **một tài khoản GitHub** (miễn phí).

Thời gian: khoảng 30 phút cho lần đầu. Sau đó mỗi lần tạo lại ứng dụng chỉ mất 10 phút.

---

## PHẦN 1. CHUẨN BỊ (chỉ làm một lần)

### Bước 1. Tạo tài khoản GitHub (nếu chưa có)
1. Mở trình duyệt (Chrome, Edge, Safari…) trên máy tính.
2. Vào địa chỉ: **github.com**
3. Bấm nút **Sign up** (góc trên bên phải).
4. Điền email, mật khẩu, tên đăng nhập. Làm theo các bước trên màn hình.
5. GitHub gửi **một mã số** về email. Mở email, chép mã đó vào GitHub.
6. Xong. Nếu đã có tài khoản rồi thì bấm **Sign in** để đăng nhập.

### Bước 2. Giải nén file dự án
1. Bạn đã nhận được file **SavifyKaraoke.zip**. Lưu nó vào máy tính (ví dụ ở màn hình Desktop).
2. **Windows:** bấm chuột phải vào file → chọn **Extract All…** → bấm **Extract**.
   **Mac:** bấm đúp vào file zip.
3. Sẽ có một thư mục tên **KaraokeApp**. Mở thư mục này ra. Bên trong bạn sẽ thấy các thứ như `app`, `build.gradle.kts`, `karaoke.jks`, `HUONG_DAN.md`…

> Quan trọng: phải **giải nén** trước. GitHub không đọc được file zip.

---

## PHẦN 2. ĐƯA CODE LÊN GITHUB

### Bước 3. Tạo "kho" (repository) mới
1. Đăng nhập GitHub. Bấm dấu **+** ở góc trên bên phải → chọn **New repository**.
2. Ô **Repository name**: gõ `savify-karaoke`
3. Chọn **Public** (công khai). *Chọn Public để việc build miễn phí và không giới hạn.*
4. **Không** tích vào các ô khác.
5. Bấm nút xanh **Create repository** ở dưới cùng.

### Bước 4. Tải các file lên bằng cách kéo thả
1. Ở trang vừa hiện ra, tìm dòng chữ **"uploading an existing file"** (chữ màu xanh, nằm trong câu "…or **uploading an existing file**"). Bấm vào đó.
2. Mở thư mục **KaraokeApp** trên máy tính (đã giải nén ở Bước 2). Nhấn **Ctrl + A** (Mac: **Cmd + A**) để chọn **tất cả** mọi thứ bên trong.
3. **Kéo** tất cả vào khung có chữ "Drag files here" trên trang GitHub, rồi thả chuột.
4. Chờ thanh tiến trình chạy xong (khoảng 1–2 phút). Danh sách file sẽ hiện ra bên dưới.
5. Kéo xuống dưới cùng, bấm nút xanh **Commit changes**.

### Bước 5. Kiểm tra: thư mục `.github` đã lên chưa?
Trên trang chính của kho, bạn phải thấy các mục: **.github**, **app**, **build.gradle.kts**, **karaoke.jks**, **settings.gradle.kts** …

- ✅ Nếu thấy **.github** → sang Phần 3.
- ❌ Nếu **không thấy `.github`** (do máy tính ẩn thư mục có dấu chấm ở đầu), làm cách này:
  1. Bấm **Add file** → **Create new file**.
  2. Ở ô tên file, gõ chính xác: `.github/workflows/build-apk.yml` (khi gõ dấu `/` GitHub tự tạo thư mục).
  3. Mở file `build-apk.yml` trong thư mục của bạn bằng **Notepad** (hoặc TextEdit), chọn tất cả (Ctrl+A), sao chép (Ctrl+C).
     *(Trên Mac/Windows file này nằm trong `KaraokeApp/.github/workflows/`. Nếu không thấy, hãy nhờ người thân bật "hiện file ẩn".)*
  4. Dán (Ctrl+V) vào khung lớn trên GitHub.
  5. Bấm **Commit changes…** → **Commit changes**.

> Nếu thiếu file **karaoke.jks** cũng không sao, hệ thống sẽ tự tạo. (Chỉ có điều: mỗi lần build lại, ứng dụng sẽ có "chữ ký" khác, nên khi cài bản mới phải gỡ bản cũ trước.)

---

## PHẦN 3. BẤM NÚT ĐỂ TẠO ỨNG DỤNG

### Bước 6. Chạy "Build APK"
1. Trên trang của kho, bấm chữ **Actions** (thanh menu phía trên, cạnh Pull requests).
2. Nếu GitHub hỏi *"I understand my workflows, go ahead and enable them"* → bấm nút đó.
3. Ở cột bên trái bấm **Build APK**.
4. Bên phải, bấm nút xám **Run workflow** → trong khung nhỏ hiện ra bấm nút xanh **Run workflow**.
5. Đợi vài giây, trang sẽ hiện một dòng mới có **vòng tròn vàng xoay** (đang chạy).

### Bước 7. Đợi khoảng 5–10 phút
- Vòng tròn vàng xoay = đang làm. **Cứ để đó, không cần bấm gì.**
- Khi xong: hiện **dấu tích xanh ✔** = thành công.
- Nếu hiện **dấu X đỏ ✘** = có lỗi → xem Phần 6.

---

## PHẦN 4. TẢI FILE ỨNG DỤNG (karaoke.apk)

Có hai cách. **Cách A dễ hơn.**

### Cách A: Tải từ mục Releases (khuyên dùng)
1. Quay về trang chính của kho (bấm tên `savify-karaoke` ở trên cùng).
2. Nhìn bên phải, mục **Releases**, bấm vào bản mới nhất (tên "Savify Karaoke - bản 1").
3. Bấm vào **karaoke.apk** để tải về.

### Cách B: Tải từ trang Actions
1. Vào **Actions** → bấm vào dòng build vừa xong (có dấu ✔).
2. Kéo xuống cuối, mục **Artifacts**, bấm **karaoke-apk**. Máy sẽ tải về một file zip.
3. Giải nén file zip đó, bên trong là **karaoke.apk**.

---

## PHẦN 5. CÀI LÊN ANDROID TV VÀ SỬ DỤNG

### Bước 8. Cho phép cài ứng dụng ngoài (làm một lần trên TV)
Trên TV: **Cài đặt (Settings) → Bảo mật & hạn chế (Security & restrictions) → Nguồn không xác định (Unknown sources)** → bật cho ứng dụng sẽ dùng để cài (xem bên dưới).
*(Tên menu hơi khác nhau theo từng hãng TV / TV box. Nếu không thấy, TV sẽ tự hỏi khi cài.)*

### Bước 9. Đưa file karaoke.apk sang TV. Chọn MỘT cách:

**Cách 1 – Dùng USB (dễ hiểu nhất)**
1. Chép **karaoke.apk** vào USB.
2. Cắm USB vào TV / TV box.
3. Mở ứng dụng **Quản lý file** (File Manager) trên TV, vào USB, chọn **karaoke.apk** → **Cài đặt (Install)**.

**Cách 2 – Dùng ứng dụng "Downloader" (không cần USB)**
1. Trên TV, vào kho ứng dụng (Google Play), tìm **Downloader** (của AFTVnews) và cài.
2. Mở Downloader, gõ địa chỉ tải file karaoke.apk (là đường dẫn bạn nhận được khi bấm chuột phải vào **karaoke.apk** ở Releases → *Copy link address*).
3. Downloader tải xong sẽ hỏi cài đặt → bấm **Install**.

**Cách 3 – Gửi bằng "Send Files to TV"**
Cài ứng dụng *Send Files to TV* trên cả điện thoại và TV, gửi file apk từ điện thoại sang TV, rồi bấm cài.

### Bước 10. Mở ứng dụng và hát!
1. Mở ứng dụng **Savify Karaoke** trên TV. **Một bài hát sẽ tự phát** sau vài giây (cần có mạng).
2. Khi chưa phát bài, **mã QR** to hiện ở góc trên bên phải. Khi đang phát bài, QR được ẩn đi (bấm **OK** trên remote TV để hiện lại bảng mã QR).
3. Dùng **điện thoại** (cùng Wi-Fi với TV): mở **Camera** → hướng vào mã QR → bấm vào đường link hiện ra.
4. Trên điện thoại hiện ra màn hình điều khiển:
   - Gõ tên bài / ca sĩ vào ô **Tìm bài hát** → bấm dấu **+** để thêm bài vào danh sách.
   - Hàng nút phía dưới: ▶/⏸ (phát/dừng), ⏭ (bài kế), ⟳ (hát lại), 🔉🔊 (âm lượng), ☰♪ (😊 **Bộ sưu tập** hiệu ứng), ✎ (dán link YouTube). Xem danh sách bài đã chọn ở tab **DANH SÁCH** phía trên.
   - Bấm **♡ Yêu thích** dưới mỗi bài để lưu vào tab **♥ YÊU THÍCH** (lưu ngay trên điện thoại này, lần sau mở lại vẫn còn).
   - Bấm nút 😊 để mở **Bộ sưu tập**: thả tim, vỗ tay, pháo giấy, pháo hoa, lửa, hoa hồng, cụng ly... Hiệu ứng sẽ bay lên màn hình TV ngay lập tức. Bộ sưu tập hiện có **76 hiệu ứng** (đợt mới: núi lửa, sóng biển, dải ngân hà, pháo bông, đèn lồng, lì xì, Giáng sinh, hổ gầm, gấu trúc, ếch nhảy, đại bàng, xe đua, máy bay, nhẫn cưới, huy chương...). Ngoài ra có ô **nhắn tin lên màn hình TV** kèm **35 câu mẫu** bấm là gửi (chúc mừng năm mới, đám cưới, sinh nhật, cạn ly, tăng/giảm âm lượng...).
5. Muốn hiện mã QR khi đang phát bài: bấm nút **OK** trên remote TV → hiện bảng QR kèm dòng "Mời quét mã QR để thêm bài". Bấm **OK** lần nữa (hoặc **Back**) để đóng.

**Mẹo:**
- Điện thoại **phải cùng Wi-Fi** với TV.
- Bấm nút **Back** trên remote TV sẽ hỏi có thoát không.
- Muốn mở khóa cho điện thoại khác: dùng chính mã QR đó. Muốn "đổi khóa": bấm OK vào mã QR → **Đặt lại ghép nối**.
- Trên màn hình mã QR phóng to có nút **Cài đặt** (đổi độ phân giải, dòng chữ "Tiếp theo", trình phát…).

---

## PHẦN 6. NẾU GẶP SỰ CỐ

| Hiện tượng | Cách xử lý |
|---|---|
| Không thấy nút **Run workflow** | Bạn chưa có file `.github/workflows/build-apk.yml`. Làm lại **Bước 5** (cách ❌). |
| Build hiện **dấu X đỏ** | Bấm vào dòng đỏ → bấm vào chữ **build** ở bên trái → kéo xuống thấy dòng chữ đỏ. **Chụp màn hình (hoặc chép dòng chữ đỏ) gửi lại cho tôi**, tôi sẽ sửa code. Rất thường chỉ là lỗi nhỏ. |
| Bước "Tao ban phat hanh" bị lỗi quyền | Vào **Settings → Actions → General → Workflow permissions** → chọn **Read and write permissions** → Save → chạy lại. Hoặc lấy file bằng **Cách B**. |
| TV báo "không cài được" | Gỡ bản Savify Karaoke cũ (nếu có) rồi cài lại. Kiểm tra đã bật "Nguồn không xác định". |
| Mở app nhưng không phát bài | Kiểm tra TV có mạng Internet. Vào **OK (QR) → Cài đặt → Trình phát** và thử đổi sang **IFRAME**. |
| Điện thoại không mở được link QR | Điện thoại và TV phải **cùng một Wi-Fi** (không dùng "Wi-Fi khách"). Hoặc gõ tay địa chỉ hiện dưới mã QR phóng to. |
| Video báo lỗi / nhảy bài liên tục | YouTube thỉnh thoảng thay đổi. Chạy lại Build APK (Bước 6) — dự án dùng bản thư viện mới nhất đã ghim; hoặc nhờ tôi cập nhật. |

---

## GHI CHÚ KỸ THUẬT (có thể bỏ qua)
- Chữ ký APK: dự án kèm khóa `karaoke.jks` (mật khẩu `karaoke123`) dùng chung cho mọi lần build, nên các bản mới cài **đè** lên bản cũ được. Khóa này chỉ để cài nội bộ, đừng đăng lên cửa hàng.
- Ứng dụng dùng thư viện NewPipe Extractor để lấy luồng video (không có quảng cáo) và có trình phát dự phòng bằng YouTube IFrame. Việc dùng có thể trái điều khoản của YouTube — chỉ nên dùng cá nhân.
- Danh sách bài tự phát khi chưa chọn bài nằm trong `app/src/main/assets/seed.json`.
