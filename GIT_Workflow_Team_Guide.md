
# HƯỚNG DẪN GIT – Quy trình làm việc cho nhóm 7 người (Android FlickrBrowser)

Tài liệu này hướng dẫn **từ A→Z**: clone dự án, tạo nhánh riêng, commit/push, tạo Pull Request (PR) cho lead review & merge vào `main`, cách đồng bộ với `main`, và **các cách khôi phục khi lỡ tay**.

---

## 0) Chuẩn bị
- Cài **Git**: https://git-scm.com/downloads
- Cài **Android Studio** (nếu làm Android).
- Có tài khoản **GitHub** và được **add collaborator** vào repo.
- Repo dự án: `https://github.com/hoangdung0703/FlickrBrowser.git`

Lần đầu dùng Git trên máy:
```bash
git config --global user.name  "Tên của bạn"
git config --global user.email "email_github@example.com"
git config --global pull.rebase true   # khuyến nghị: pull dùng rebase cho lịch sử gọn
```
---

## Quy ước commit & PR (tham khảo)
**Commit message** (Angular style):
```
feat(scope): do something
fix(scope): fix something
docs: update docs
chore: clean code / config
refactor(scope): change structure w/o feature change
```
**PR title**:
```
[Feature] Explore Grid – paging + pull-to-refresh
[UI] Week 1 – Base Design System
[Fix] Timeout on search (retry/backoff)
```

**PR description** nên có:
- Tóm tắt thay đổi
- Ảnh/clip minh họa (nếu là UI)
- Checklist test nhanh
- Ảnh chụp log lỗi/đã fix (nếu bug)

---

## Lưu ý khi làm
- Không push trực tiếp vào `main`. Mọi thay đổi qua **PR(Pull Request)**.
- Nhánh cá nhân **thường xuyên rebase** lên `origin/main`.
- Mọi text cố định → `strings.xml` (chuẩn i18n). Không hardcode màu/spacing (dùng token).
- Review PR có tâm: xem code, chạy thử (nếu có thể), comment rõ ràng.


---
*ĐÂY LÀ BƯỚC ĐẦU
## 1) Clone dự án
```bash
git clone https://github.com/hoangdung0703/FlickrBrowser.git
cd FlickrBrowser
git remote -v   # kiểm tra remote 'origin' đã đúng URL
```

> **Mặc định sau khi clone, bạn đang ở nhánh `main`.**

---

## 2) Tạo nhánh riêng cho từng người - đã ghi trong file doc trên mess
**Quy ước tên nhánh** (snake-case, rõ việc làm):
- `feature/explore-grid`, `feature/search`, `feature/design-system`
- `bugfix/detail-crash`, `chore/gradle-update`

**Tạo & chuyển sang nhánh riêng (ví dụ bạn làm Design System):**
```bash
git checkout -b feature/design-system
```
Làm việc, code…

---

## 3) Commit & Push lên nhánh riêng - hỏi AI hỗ trợ thêm
**Chia commit nhỏ, message rõ ràng (theo convention):**
- `feat(ui): ...` – thêm tính năng UI
- `fix(api): ...` – sửa bug API
- `docs: ...` – cập nhật tài liệu
- `chore: ...` – dọn dẹp / config

Ví dụ:
```bash
git add app/src/main/res/values/*
git commit -m "feat(ui): setup base design system (colors, dimens, styles, theme)"
git push origin feature/design-system
```

> Lần đầu push nhánh mới, Git sẽ tạo `origin/feature/design-system` trên GitHub.

---

## 4) Tạo Pull Request (PR) để lead review & merge vào `main`
1. Lên GitHub → repo → chuyển sang nhánh của bạn (`feature/...`).
2. Bấm **Compare & pull request**.
3. Đảm bảo **base = main**, **compare = feature/...**.
4. Viết **title/description** rõ ràng, đính kèm ảnh/video nếu cần.
5. Bấm **Create pull request**.
6. Chờ **lead/owner** review. Khi đạt yêu cầu, họ sẽ **Merge pull request**.

> **Khuyến nghị:** Bật **Branch protection** cho `main` để chỉ lead/owner mới được merge.

---

## 5) Đồng bộ nhánh cá nhân với `main` mới nhất
Trong quá trình dev, `main` luôn thay đổi. Thường xuyên **rebase** nhánh cá nhân của bạn lên `main` để hạn chế conflict.

```bash
# Đứng trong nhánh cá nhân
git add .
git commit -m "WIP"            # hoặc git stash nếu không muốn commit tạm
git fetch origin
git rebase origin/main         # xếp commit của bạn lên trên main mới nhất

# Nếu có conflict: mở file, sửa phần <<<<<<< ======= >>>>>>>, sau đó:
git add <file-da-sua>
git rebase --continue

# Nếu trước đó nhánh đã push rồi, sau rebase cần:
git push --force-with-lease
```

> **Không dùng `--force` trần**; hãy dùng `--force-with-lease` để an toàn hơn.

---

## 6) Quy trình chuẩn cho cả nhóm (tóm tắt 7 bước)
1. `git clone` (lần đầu) / `git pull` (những lần sau) ở `main`.
2. `git checkout -b feature/...` tạo nhánh riêng.
3. Code → `git add` → `git commit` (nhỏ, rõ ràng).
4. `git push origin feature/...`.
5. Tạo **PR** từ nhánh của bạn → `main`.
6. Lead review → nếu OK thì **merge**.
7. Tất cả kéo về `main`: `git pull origin main`.

---

## 7) Làm việc với Android Studio (GUI)
- **Kiểm tra/đổi nhánh**: góc dưới phải hiển thị tên branch → click để switch.
- **Commit**: `Ctrl+K` / `Cmd+K`.
- **Push**: `Ctrl+Shift+K` / `Cmd+Shift+K`.
- **Update Project (pull)**: `VCS → Update Project…`.

---

## 8) Xử lý conflict cơ bản
Khi merge/rebase có conflict, Git đánh dấu trong file:
```
<<<<<<< HEAD
(code ở nhánh A)
=======
(code ở nhánh B)
>>>>>>>
```
- Chọn phần cần giữ hoặc hòa trộn.
- Xóa các dấu `<<<<<<<`, `=======`, `>>>>>>>`.
- `git add <file>` → tiếp tục merge/rebase.

---

## 9) Cứu nguy & khôi phục khi lỡ tay (UNDO/RECOVER)

### 9.1. Xem lại lịch sử & “cứu cánh”
```bash
git log --oneline --graph --decorate --all   # xem lịch sử
git reflog                                   # lịch sử di chuyển HEAD (cực mạnh để cứu)
```

### 9.2. Hoàn tác file CHƯA commit
- Hoàn tác thay đổi về trạng thái index:
```bash
git restore <file>
```
- Hoàn tác tất cả thay đổi chưa commit:
```bash
git restore --worktree .
```

### 9.3. Bỏ thay đổi đã `git add` (chưa commit)
```bash
git restore --staged <file>
```

### 9.4. Quay về commit trước (cập nhật working tree)
> **Cẩn thận**: thay đổi history local của bạn.
```bash
git reset --hard HEAD~1
```
Hoặc quay về một commit cụ thể:
```bash
git reset --hard <commit_hash>
```

### 9.5. Tạo commit đảo ngược (giữ lịch sử, an toàn khi đã push)
```bash
git revert <commit_hash>
```
→ Tạo commit mới hủy tác động của commit cũ.

### 9.6. Lưu tạm thay đổi chưa muốn commit (stash)
```bash
git stash push -m "temp-work"
git stash list
git stash pop    # lấy lại và xóa khỏi stash
# hoặc:
git stash apply  # lấy lại nhưng vẫn giữ bản trong stash
```

### 9.7. Khôi phục nhánh/commit “biến mất”
- Dùng `git reflog` tìm lại commit/branch SHA rồi checkout:
```bash
git checkout -b rescue-branch <sha-tu-reflog>
```


---

## 10) FAQ ngắn
- **Clone xong không thấy branch của bạn?**  
  `git fetch origin` → `git checkout feature/...`
- **Pull bị báo “local changes would be overwritten”?**  
  Commit hoặc `git stash` rồi thử pull lại.
- **Đang rebase/merge kẹt giữa chừng?**  
  Sửa conflict → `git add` → `git rebase --continue` (hoặc `git merge --continue`).  
  Muốn hủy: `git rebase --abort` / `git merge --abort`.
- **Lỡ xóa nhánh?**  
  `git reflog` tìm lại SHA → `git checkout -b <new-branch> <sha>`.

---

