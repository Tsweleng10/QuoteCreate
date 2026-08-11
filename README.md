# 📝 Quotation Maker App

A full-featured Android app for creating professional quotes with automatic calculations and PDF export. Built with Java, Room, and iText 7.

![App Demo](https://via.placeholder.com/800x400.png?text=Quote+App+Demo)


---

## 🚀 Features

- **Company Profile Setup** – Save your company details once (Name, Registration No, Tax No, Address).
- **Dynamic Quote Creation** – Add/remove line items with descriptions, quantity, and rate.
- **Auto-Calculation** – Amount = Quantity × Rate, total sum updated instantly.
- **Quote Numbering** – Auto-incrementing zero-padded sequence (e.g., `SDQU-00123`).
- **PDF Generation** – Exports a beautifully formatted A4 PDF matching the design spec (logo, header, line-item table, footer disclaimer).
- **Quote History** – View, edit, duplicate, or delete past quotes.
- **Share PDF** – Share generated PDFs via email, WhatsApp, or any other app.

---

## 🛠️ Tech Stack

- **Language**: Java
- **Architecture**: MVVM (Model-View-ViewModel) with Room for local persistence
- **Database**: Room (SQLite) with DAOs
- **PDF Generation**: iText 7 Community Edition
- **UI**: Material Design Components
- **Minimum SDK**: API 24 (Android 7.0 Nougat)
- **Target SDK**: API 33 (Android 13)

---

## 📁 Project Structure

```
app/src/main/java/com/example/quotecreate/
├── activities/
│   ├── CompanyProfileActivity.java
│   ├── QuoteListActivity.java
│   └── QuoteFormActivity.java
├── database/
│   ├── AppDatabase.java
│   ├── CompanyDao.java
│   ├── QuoteDao.java
│   ├── LineItemDao.java
│   └── QuoteSequenceDao.java
├── models/
│   ├── Company.java
│   ├── Quote.java
│   ├── LineItem.java
│   └── QuoteSequence.java
└── utils/
    └── PdfGenerator.java
```

---

## 🧪 How to Run the App

1. **Clone the repository** (if using Git):
   ```bash
   git clone https://github.com/yourusername/quote-create.git
   ```
2. **Open in Android Studio** – File → Open → select the project folder.
3. **Sync Gradle** – Click "Sync Now" when prompted.
4. **Run on Emulator or Physical Device** – Select a device and click the green ▶ button.

---

## 📦 How to Build an APK

In Android Studio:

1. Build → Build Bundle(s) / APK(s) → Build APK(s)
2. The APK will be generated at:
   ```
   app/build/outputs/apk/debug/app-debug.apk
   ```

---

## 🎯 Usage Guide

1. **First Launch** – You'll be prompted to set up your company profile. Fill in the details and save.
2. **Create a Quote** – Tap the "+" FAB button. Enter the project reference, date, and default rate.
3. **Add Line Items** – Tap "Add Row". Fill in description, quantity, and rate. Amounts auto-calculate.
4. **Save or Generate PDF** – Save the quote for later editing, or generate a PDF with the current data.
5. **View History** – From the main screen, see all quotes. Tap any to edit or long-press to delete.
6. **Share PDF** – After generating, the PDF is automatically shared via your preferred app.

---

## 🖼️ PDF Output Example

The generated PDF follows this layout:

- **Header** – Company logo (icon + "SEAKO" + tagline) and metadata (company info, quote number, date, reference).
- **Line-Item Table** – Columns: #, Description, Qty, Rate, Amount (with R prefix, thousands separators, 2 decimals). Total row with double underline.
- **Footer** – Disclaimer text (can be customised).

---

## 🤝 Contributing

Contributions are welcome! Please open an issue or submit a pull request.

---

## 📄 License

This project is open-source and available under the MIT License.

---

## 📱 Demo Video

👉 [Click here to watch the app in action](#)
*(Replace with your actual YouTube link)*

---

## 💬 Contact

For any questions or feedback, please reach out to [Your Email] or open an issue on GitHub.

---

Built with ❤️ using Android Studio & Java.

---

