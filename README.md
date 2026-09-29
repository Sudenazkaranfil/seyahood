# Seyahood — Backend

Spring Boot ile geliştirilmiş RESTful API. Kullanıcı kimlik doğrulama, ajanda yönetimi, canvas editör verisi, fotoğraf yükleme, harita entegrasyonu ve Plus/PRO abonelik sistemini sağlar.

## Teknolojiler

- Java 21 + Spring Boot 4.1
- PostgreSQL + Spring Data JPA / Hibernate (prod: Supabase)
- JWT (JSON Web Token) kimlik doğrulama
- Cloudinary (fotoğraf depolama)
- Resend (e-posta doğrulama / şifre sıfırlama)
- OpenStreetMap / Nominatim (geocoding)
- Google Cloud Vision API (ünlü yer/anıt tanıma)
- Anthropic Claude API (genel amaçlı görsel konum tahmini, son çare)
- RevenueCat webhook (abonelik satın alma bildirimleri)
- Lombok, Spring Security, Spring Validation

## Özellikler

- Kullanıcı kayıt, giriş, e-posta doğrulama, şifre sıfırlama (JWT)
- Ajanda oluşturma, güncelleme, silme (public/private)
- Canvas editör verisi (drag-drop elementler, çizimler, fontlar) JSON olarak saklama
- Fotoğraf yükleme (Cloudinary entegrasyonu)
- Konum tabanlı entry'ler (lat/lng geocoding) ve public konum haritası
- **AI destekli konum tahmini** (PRO): EXIF → Google Vision (ücretsiz, ünlü yerler) → Claude (ücretli, genel tahmin) katmanlı zinciri
- Public ajanda keşfet, arama ve popüler kullanıcılar
- Takip sistemi (follow/followers) ve başkasının ajandasını kaydetme
- Kullanıcı profil yönetimi
- **Plus/PRO abonelik sistemi**: plan bazlı limitler (ajanda/sayfa sayısı, export kalitesi, fontlar, şablonlar), RevenueCat webhook entegrasyonu
- **Promosyon kodu sistemi**: admin korumalı, plan/süre/kullanım limiti özelleştirilebilir kodlar (hediye/indirim/test amaçlı)

## Kurulum

### Gereksinimler
- Java 21+
- PostgreSQL 17
- Maven

### Adımlar

1. Repoyu klonla
```bash
git clone https://github.com/Sudenazkaranfil/seyahood.git
cd seyahood
```

2. PostgreSQL'de veritabanı oluştur
```sql
CREATE DATABASE seyahood;
```

3. `src/main/resources/application.properties` dosyası oluştur
```properties
spring.application.name=seyahood
spring.datasource.url=jdbc:postgresql://localhost:5432/seyahood
spring.datasource.username=postgres
spring.datasource.password=YOUR_PASSWORD
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
jwt.secret=YOUR_JWT_SECRET
jwt.expiration=86400000
app.admin-key=YOUR_ADMIN_KEY
cloudinary.cloud-name=YOUR_CLOUD_NAME
cloudinary.api-key=YOUR_API_KEY
cloudinary.api-secret=YOUR_API_SECRET
resend.api.key=YOUR_RESEND_API_KEY
anthropic.api.key=YOUR_ANTHROPIC_API_KEY
google.vision.api.key=YOUR_GOOGLE_VISION_API_KEY
```
`app.admin-key`, `anthropic.api.key` ve `google.vision.api.key` opsiyoneldir — sırasıyla promosyon kodu yönetimi ve AI konum tahmininin ücretli/ücretsiz katmanları için gereklidir, boş bırakılırsa ilgili özellikler sessizce devre dışı kalır.

4. Uygulamayı başlat
```bash
mvn spring-boot:run
```

## API Endpoints

### Auth
| Method | URL | Açıklama |
|--------|-----|----------|
| POST | /auth/register | Kayıt ol |
| POST | /auth/login | Giriş yap |
| GET | /auth/profile | Profil bilgisi |
| PUT | /auth/profile | Profil güncelle |
| POST | /auth/profile/image | Profil fotoğrafı yükle |
| POST | /auth/verify | E-posta doğrula |
| POST | /auth/resend-verification | Doğrulama kodunu tekrar gönder |
| POST | /auth/forgot-password | Şifre sıfırlama kodu iste |
| POST | /auth/verify-reset-code | Sıfırlama kodunu doğrula |
| POST | /auth/reset-password | Şifreyi sıfırla |

### Journals
| Method | URL | Açıklama |
|--------|-----|----------|
| GET | /journals | Ajandalarım |
| POST | /journals | Ajanda oluştur |
| PUT | /journals/{id} | Ajanda güncelle |
| DELETE | /journals/{id} | Ajanda sil |
| GET | /journals/public | Public ajandalar |
| GET | /journals/saved | Kaydettiğim ajandalar |
| POST | /journals/{id}/save | Ajandayı kaydet/kaydı kaldır |
| GET | /journals/{id}/saved | Kaydedilmiş mi kontrol et |
| POST | /journals/{id}/view | Görüntülenme sayısını artır |
| POST | /journals/{id}/cover | Kapak rengi/görseli ayarla |

### Entries
| Method | URL | Açıklama |
|--------|-----|----------|
| GET | /journals/{id}/entries | Sayfalar |
| POST | /journals/{id}/entries | Sayfa oluştur |
| PUT | /journals/{id}/entries/{entryId} | Sayfa güncelle |
| DELETE | /journals/{id}/entries/{entryId} | Sayfa sil |
| GET | /entries/public-locations | Public konum haritası verisi |

### Photos
| Method | URL | Açıklama |
|--------|-----|----------|
| POST | /entries/{id}/photos | Fotoğraf yükle |
| POST | /photos/estimate-location | AI ile konum tahmini (PRO) |

### Users
| Method | URL | Açıklama |
|--------|-----|----------|
| GET | /users/search | Kullanıcı ara |
| GET | /users/{username} | Kullanıcı profili |
| POST | /users/{username}/follow | Takip et / bırak |
| GET | /users/{username}/follow-status | Takip durumu |
| GET | /users/{username}/followers | Takipçiler |
| GET | /users/{username}/following | Takip edilenler |
| GET | /users/popular | Popüler kullanıcılar |

### Subscription
| Method | URL | Açıklama |
|--------|-----|----------|
| GET | /subscription/status | Plan durumu (Free/Plus/PRO) |
| POST | /subscription/webhook | RevenueCat satın alma bildirimi |
| POST | /subscription/register-customer | RevenueCat müşteri ID kaydı |
| POST | /subscription/redeem-code | Promosyon kodu kullan |
| POST/GET | /subscription/admin/codes | Kod oluştur / listele (admin) |
| POST | /subscription/admin/codes/{id}/deactivate | Kodu devre dışı bırak (admin) |
