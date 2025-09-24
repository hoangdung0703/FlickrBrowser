# Hướng dẫn Giao diện cho FlickrBrowser
Định nghĩa **Design System** cho ứng dụng Android FlickrBrowser.  
Tất cả cần tuân thủ để giao diện đồng nhất.
Dùng cho Explore/Search/Favorites cơ bản, còn update tiếp file khác.
---

## 1. Màu sắc (`res/values/colors.xml`)
Sử dụng clone y hệt flickr, thấy cái nào màu nào thì gọi ở đây.
| Token | Giá trị | Khi sử dụng | Giải thích |
|-------|---------|-------------|------------|
| `@color/md_theme_primary` | #0063DC | Nút chính, tab indicator, link | Màu xanh thương hiệu Flickr |
| `@color/md_theme_onPrimary` | #FFFFFF | Chữ/icon trên nền xanh | Giúp chữ/icon nổi rõ trên nền primary |
| `@color/md_theme_secondary` | #FF0084 | Chip active, badge, điểm nhấn nhỏ | Màu hồng thương hiệu Flickr |
| `@color/md_theme_onSecondary` | #FFFFFF | Chữ/icon trên nền hồng | Giữ độ tương phản, dễ đọc |
| `@color/md_theme_background` | #FFFFFF | Nền màn hình | Nền trắng phẳng giống Flickr |
| `@color/md_theme_onBackground` | #111827 | Văn bản chính | Xám đậm, dễ đọc hơn đen thuần |
| `@color/md_text_secondary` | #4B5563 | Văn bản phụ, metadata | Xám nhạt hơn, dùng cho thông tin phụ |
| `@color/md_theme_outline` | #E5E7EB | Đường viền, divider, border card | Xám rất nhạt, tạo tách biệt nhẹ |
| `@color/md_scrim` | #80000000 | Lớp phủ mờ khi zoom ảnh, mở dialog | Màu đen 50% trong suốt |

---

## 2. Kiểu chữ (`res/values/styles.xml`)

| Style | Kế thừa | Khi sử dụng | Giải thích |
|-------|---------|-------------|------------|
| `@style/Text.Flickr.Title` | TitleLarge | Tiêu đề màn hình, tiêu đề section | ~22sp, in đậm, giống tiêu đề “Explore” |
| `@style/Text.Flickr.Body` | BodyMedium | Nội dung chính | ~14sp, dễ đọc |
| `@style/Text.Flickr.Caption` | BodySmall | Metadata (người đăng, ngày, lượt xem) | ~12sp, màu xám phụ |

---

## 3. Khoảng cách & kích thước (`res/values/dimens.xml`)
Khi dùng cái này cần kết hợp nhìn app flickr để biết đc khoảng cách cần dùng cái nào cho hợp lí, nên hỏi AI.

| Token | Giá trị | Khi sử dụng | Giải thích |
|-------|---------|-------------|------------|
| `@dimen/spacing_xs` | 4dp | Khoảng cách rất nhỏ (icon + text) | Tạo nhịp chặt chẽ |
| `@dimen/spacing_s` | 8dp | Padding card, grid ảnh | Khoảng cách chuẩn Material |
| `@dimen/spacing_m` | 12dp | Khoảng cách giữa các card | Thoáng vừa đủ |
| `@dimen/spacing_l` | 16dp | Padding màn hình, container lớn | Tạo khoảng thoáng |
| `@dimen/spacing_xl` | 24dp | Khoảng cách cho header, section | Giúp layout thoáng |
| `@dimen/radius_m` | 8dp | Bo góc card, ảnh | Bo nhẹ, giống UI Flickr |
| `@dimen/elev_s` | 2dp | Độ nổi card | Bóng mờ nhẹ, tinh tế |

Ví dụ:
<TextView
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:padding="@dimen/spacing_s"
    android:layout_marginTop="@dimen/spacing_m" -> chúng m gọi dimens + giá trị. 
    android:text="Hello Flickr!" />

---

## 4. Kiểu Card (`res/values/styles.xml`)

```xml
<style name="Widget.Flickr.Card" parent="Widget.Material3.CardView.Elevated">
    <item name="cardCornerRadius">@dimen/radius_m</item> - dùng để bo góc cho card
    <item name="cardElevation">@dimen/elev_s</item> bóng đổ của card so với nền
    <item name="android:foreground">?attr/selectableItemBackground</item> hiệu ứng nhấp nháy khi bấm
    <item name="strokeColor">@color/md_theme_outline</item> màu viền của card.
    <item name="strokeWidth">0dp</item> độ dày viền card.
</style>
```

- Sử dụng cho grid ảnh, item list, hoặc container card.  
- Bo góc 8dp, elevation 2dp.  
- Có ripple khi click.  
- Viền xám nhạt để phân tách.

---

## 5. Ví dụ sử dụng

### Card grid ảnh
```xml
<com.google.android.material.card.MaterialCardView
    style="@style/Widget.Flickr.Card"
    android:layout_margin="@dimen/spacing_m">

    <ImageView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:src="@drawable/sample"/>
</com.google.android.material.card.MaterialCardView>
```

### Metadata caption
```xml
<TextView
    style="@style/Text.Flickr.Caption"
    android:textColor="@color/md_text_secondary"
    android:text="by John Doe · 2 days ago"/>
```

---

## 6. Quy tắc
- **Không hardcode** mã màu (#hex) hoặc số dp/sp trực tiếp. Luôn dùng token.  
- **Xanh (#0063DC)**: dùng cho action chính, link, tab indicator.  
- **Hồng (#FF0084)**: chỉ dùng làm accent nhỏ (chip, badge).  
- Nền luôn **trắng**, text chính xám đậm, metadata xám nhạt.  
- Luôn tái sử dụng style card, spacing, text appearance để giao diện đồng bộ.  


