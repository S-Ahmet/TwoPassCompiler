# İki Geçişli Derleyici (Two Pass Compiler)

Bu proje, Compiler Design (Derleyici Tasarımı) dersi kapsamında Java dili kullanılarak geliştirilmiştir.

## Proje Amacı

Kaynak kodu analiz eden iki geçişli (Two Pass) bir derleyici geliştirmek.

Proje iki temel aşamadan oluşmaktadır:

### Pass 1 - Lexical Analysis (Sözcüksel Analiz)

Bu aşamada:

* Kaynak kod okunur.
* Token'lar oluşturulur.
* Anahtar kelimeler (Keywords) belirlenir.
* Tanımlayıcılar (Identifiers) belirlenir.
* Sayısal sabitler (Numeric Literals) belirlenir.
* Metinsel sabitler (String Literals) belirlenir.
* Operatörler ve ayırıcılar belirlenir.
* Symbol Table oluşturulur.
* Lexical hatalar tespit edilir.

### Pass 2 - Syntax ve Semantic Analysis

Bu aşamada:

* Sözdizimi (Syntax) kontrol edilir.
* AST (Abstract Syntax Tree) oluşturulur.
* Değişkenlerin tanımlanıp tanımlanmadığı kontrol edilir.
* Veri tipi uyumluluğu kontrol edilir.
* Semantic hatalar tespit edilir.

## Program Özellikleri

* Token Listeleme
* Symbol Table Oluşturma
* AST (Abstract Syntax Tree) Gösterimi
* Lexical Error Kontrolü
* Syntax Error Kontrolü
* Semantic Error Kontrolü
* Satır - Token Eşleştirme
* Grafiksel Kullanıcı Arayüzü (GUI)

## Kullanılan Teknolojiler

* Java
* Java Swing
* IntelliJ IDEA

## Geliştirici

Sadık Ahmet Karabulut
