# Program lojalnościowy sieci "Razem" — ćwiczenie modelarskie

## Kontekst systemu

System składa się z czterech modułów:

```
┌─────────────────────────────────────────────────────────────┐
│                          System                             │
│                                                             │
│  ┌─────────────┐   ┌─────────────┐   ┌─────────────────┐    │
│  │   Member    │   │  Campaign   │   │  Notification   │    │
│  │             │   │             │   │                 │    │
│  │ rejestracja │   │  promocje   │   │  email / push   │    │
│  │ weryfikacja │   │  kupony     │   │  SMS            │    │
│  │ profile     │   │  rabaty     │   │                 │    │
│  └─────────────┘   └─────────────┘   └─────────────────┘    │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐    │
│  │  ██████████████  Loyalty  ██████████████            │    │
│  │                                                     │    │
│  │         ← tutaj pracujemy →                         │    │
│  │                                                     │    │
│  └─────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────┘
```

Uczestnicy programu są już zarejestrowani i zweryfikowani w module `Member`.
Promocje i kupony zarządzane są w module `Campaign`.
Powiadomienia (email, push, SMS) obsługuje moduł `Notification`.

**Nasz kod powstaje wyłącznie w module `Loyalty`.**

---

## Wymagania biznesowe

Celem biznesowym jest implementacja portfeli lojalnościowych w module `Loyalty` dla sieci sklepów "Razem".

**Założenia kontekstowe:**
System jest już istniejący. Uczestnicy programu są zarejestrowani i zweryfikowani w module `Member`.
Skupiamy się wyłącznie na module `Loyalty`.

---

### Uczestnictwo w programie

Przystąpienie do programu skutkuje natychmiastową aktywacją portfela lojalnościowego.
Portfel **otrzymuje i przechowuje** unikalny numer w momencie otwarcia.
Każdy uczestnik może mieć tylko jeden portfel.

Portfel **posiada dane właściciela**:
- numer członkowski (zgodny z zasadami walidacji — suma kontrolna),
- imię i nazwisko (nazwisko minimum 2 znaki),
- datę urodzenia (musi znajdować się w przeszłości),
- adres e-mail (poprawny format),
- numer telefonu (poprawny format krajowy).

Dane te można aktualizować w dowolnym momencie.

Portfel **przechowuje listę osób** z dostępem (np. członków rodziny) — wszystkie osoby
z dostępem korzystają ze wspólnego salda.

---

### Saldo punktów

Portfel **trzyma saldo punktów** oraz **przechowuje informację** o tym ile punktów jest dostępnych
do realizacji. Aktywny portfel jest gotowy do realizacji punktów.

---

### Realizacja punktów

Uczestnik realizuje punkty wskazując ile chce wykorzystać. Realizacja jest możliwa wyłącznie gdy:
- portfel jest aktywny,
- żądana liczba punktów nie przekracza dostępnego salda.

Portfel **ma przypisaną politykę korzyści**, którą **przechowuje**. Polityka może obniżyć
faktycznie pobierane saldo — np. portfel Gold faktycznie zużywa 5% mniej punktów niż
realizuje (realizuje 100, ze salda schodzi 95). Różne polityki stosują różne algorytmy.

Po każdej realizacji zewnętrzny system analityczny sieci otrzymuje stosowną informację.

---

### Doładowanie portfela

Punkty trafiają do portfela w wyniku zakupów w sieci lub u uczestniczących sprzedawców.
Doładowanie może nastąpić w dowolnym momencie. Nowe punkty są natychmiast dostępne
do realizacji. Po doładowaniu uczestnik otrzymuje powiadomienie push.

Wyliczenie należnych punktów jest złożone — zależy od:
- wartości zakupu (inne przeliczniki dla różnych przedziałów kwotowych),
- poziomu uczestnika (Standard, Silver, Gold, Platinum),
- kanału transakcji (sklep stacjonarny, aplikacja mobilna, sklep internetowy),
- pory dnia (zakupy wieczorne 18:00–22:00 mają mnożnik bonusowy),
- aktywnych promocji przypisanych do produktu lub kategorii.

Wynik obliczeń jest rozkładany na wielu odbiorców według reguł powiązanych z daną transakcją.
Zakup może skutkować doładowaniem portfela kupującego, osoby polecającej, operatora programu
i innych uczestników — każdemu według osobnego algorytmu i innej kalkulacji. Liczba odbiorców
z jednej transakcji nie jest z góry ustalona. Każdy odbiorca ma własny portfel, na który trafia
jego porcja punktów.

---

### Okno realizacji

Portfel **ma tygodniowe okno realizacji** — maksymalną liczbę realizacji w okresie. Czyli mogę uży portfela X razy w ciągu tygodnia. 
Portfel **przechowuje kolekcję okien realizacji** wraz z historią realizacji w każdym oknie.
Większość uczestników ma okno **tygodniowe**, jednak niektórzy wynegocjowali okno **miesięczne**
— obowiązuje ono zamiast tygodniowego. Realizacja jest możliwa tylko gdy bieżące okno
nie jest wyczerpane. W danej chwili aktywne jest jedno okno.

Portfel przechowuje pełną historię realizacji i doładowań. Portfel grupuje realizacje to po oknach realizacji

---

### Zamknięcie okna

Po upływie okresu okna generowany jest raport aktywności. Brak jakiejkolwiek aktywności
przez 4 kolejne okna skutkuje dezaktywacją portfela.

---

### Wymagania krytyczne

- realizacja punktów ponad dostępne saldo jest niedopuszczalna,
- doładowanie musi być natychmiast widoczne przy kolejnych realizacjach,
- system musi obsługiwać dużą liczbę równoczesnych realizacji od wielu uczestników.

---
---

## Pułapki modelarskie

### 1. Agregat jako worek na powiązane dane

**Skąd wynika:** z języka wymagań — "portfel *posiada*, *trzyma*, *przechowuje*, *ma przypisane*".
Każde z tych słów sugeruje zawieranie. Naturalny odruch: jeden duży `LoyaltyWallet` z wszystkim w środku.

**Dlaczego źle:** agregat to granica blokowania, nie struktura danych. Pytanie nie brzmi
"co portfel posiada?" tylko "co musi być zablokowane razem podczas realizacji?".

**Sygnał w kodzie:**
```java
class LoyaltyWallet {
    MemberProfile owner;         // dane uczestnika
    List<WalletAccess> accesses; // dostępy
    List<RealizationWindow> windows; // okna z historią

    BenefitPolicy policy;        // polityka rabatowa
    int balance;
    int availableBalance;
}
```

---

### 2. Dane uczestnika w agregacie

**Skąd wynika:** "portfel *posiada dane właściciela*" — wymaganie dosłownie mówi że portfel
to trzyma.

**Dlaczego źle:** dane uczestnika (numer członkowski, imię, email, telefon) mają wyłącznie
reguły walidacyjne przy zapisie. Żadna z nich nie uczestniczy w niezmienniku realizacji.
Blokowanie danych osobowych razem z saldem to zbędny narzut.

**Poprawnie:** `MemberProfile` to encja CRUD z walidacją. `LoyaltyWallet` trzyma tylko `memberId`.

---

### 4. Okno realizacji jako zagnieżdżony agregat

**Skąd wynika:** "portfel *przechowuje kolekcję okien realizacji* wraz z historią realizacji
w każdym oknie" — wymaganie sugeruje bogatą strukturę po write side.

**Dlaczego źle:** przy realizacji potrzebujemy tylko wiedzieć "czy licznik < limit". Do tego
wystarczy jeden integer. Pełna historia w oknie to reguła prezentacyjna — należy do read side.

**Poprawnie:**
```java
// write side — minimalnie
class LoyaltyWallet {
    int windowRealizationCount; // tylko licznik
    int windowLimit;
}

// read side — projekcja z eventów
class ActivityReport {
    // grupuje PointsRealized eventy w okna na podstawie timestampów
}
```

---

### 5. Rytm resetu okna w agregacie

**Skąd wynika:** wymaganie mówi że "niektórzy mają okno miesięczne, inni tygodniowe".
Naturalny odruch: agregat sprawdza datę i sam decyduje kiedy resetować.

**Dlaczego źle:** agregat zna rytm — logika "tygodniowy vs miesięczny" wcieka do środka.
Każdy nowy rytm wymaga zmiany agregatu.

**Sygnał w kodzie:**
```java
public void realize(int amount) {
    if (windowType == MONTHLY) {
        if (daysSinceWindowOpen() > 30) resetWindow();
    } else {
        if (daysSinceWindowOpen() > 7) resetWindow();
    }
    // ...
}
```

**Poprawnie:** zewnętrzny scheduler zna rytm per portfel i wysyła `WindowResetCommand`.
Agregat tylko zeruje licznik — nie wie czy to tydzień czy miesiąc.

---

### 6. Dostępy (shares) poza agregatem

**Skąd wynika:** dostępy wyglądają jak osobna odpowiedzialność — można je wydzielić
do `WalletAccess` i odpytać przed realizacją.

**Dlaczego źle:** race condition — ktoś odbiera dostęp dokładnie między sprawdzeniem
dostępu a realizacją. Jeden z nich przegrywa. Rozwiązanie: process aggregate blokujący
oba obiekty naraz — zbędna złożoność.

**Poprawnie:** lista uprawnionych żyje w `LoyaltyWallet`. Sprawdzenie dostępu i realizacja
to jedna transakcja, jeden zamek. Niezmiennik "tylko uprawniony może realizować" wymaga
shares wewnątrz agregatu.

---

### 7. Polityka korzyści w agregacie

**Skąd wynika:** "portfel *ma przypisaną politykę korzyści*, którą *przechowuje*".

**Dlaczego źle:** obliczenie rabatu to reguła obliczeniowa, nie spójnościowa. Nie wymaga
blokady. Algorytm w agregacie — przy każdym nowym poziomie zmieniamy agregat.

**Sygnał w kodzie:**
```java
public void realize(int requestedAmount) {
    int finalAmount = requestedAmount;
    if (policy == GOLD) finalAmount = (int)(requestedAmount * 0.95);
    else if (policy == PLATINUM) finalAmount = (int)(requestedAmount * 0.90);
    this.availableBalance -= finalAmount;
}
```

**Poprawnie:** `BenefitPolicy.apply(requestedAmount, policyType)` → `finalAmount`.
Czysta funkcja wywołana przed wejściem do agregatu. Agregat dostaje gotową liczbę.

---

### 8. Obliczenia doładowania w agregacie lub serwisie

**Skąd wynika:** logika jest złożona i "należy" do operacji doładowania — ktoś wciśnie ją
do serwisu aplikacyjnego jako serię ifów, albo do samego agregatu.

**Dlaczego źle:**
```java
// serwis aplikacyjny
int points = (int)(purchase.amount() * BASE_RATE);
if (purchase.isEvening()) points = (int)(points * 1.5);
if (member.level() == GOLD) points = (int)(points * 1.2);
if (promotion != null) points += promotion.bonus();
wallet.topUp(points);
friendWallet.topUp(friendPoints); // skąd friendPoints? kolejne ify
```

Brak historycznej odtwarzalności. Brak breakdownu. Przy każdej nowej regule — zmieniamy serwis.

**Poprawnie:** pricing engine (`PointsCalculator`) z `ComponentBreakdown` — różne kalkulatory
per przedział kwotowy, poziom, kanał, pora dnia, promocja. Serwis dostaje breakdown i iteruje.

---

### 9. Zakodowana liczba odbiorców doładowania

**Skąd wynika:** w głowie mamy "kupujący i polecający" — dwie osoby, łatwo zakodować.

**Dlaczego źle:**
```java
ComponentBreakdown breakdown = calculator.calculate(context);
wallet.topUp(breakdown.getCustomerComponent().amount());
referrerWallet.topUp(breakdown.getReferrerComponent().amount()); // a jak jest 10 odbiorców?
```

Wymaganie wprost mówi: "liczba odbiorców nie jest z góry ustalona".

**Poprawnie:**
```java
ComponentBreakdown breakdown = calculator.calculate(context);
for (PointComponent component : breakdown.components()) {
    LoyaltyWallet target = walletRepository.findById(component.targetWalletId());
    target.topUp(component.amount());
}
```

---

### 10. Logika procesowa w agregacie

**Skąd wynika:** "po realizacji system analityczny otrzymuje informację" — brzmi jak część operacji realizacji.

**Dlaczego źle:**
```java
public void realize(int amount) {
    this.availableBalance -= finalAmount;
    analyticsClient.notify(this.id, amount); // blokada trwa przez HTTP call
    pushService.send(this.ownerId, "...");    // efekt uboczny ukryty w agregacie
}
```

Blokada trwa przez czas wywołań zewnętrznych. Agregat zależy od infrastruktury.
Niemożliwy do testowania bez mocków.

**Poprawnie:** agregat emituje event i kończy. Handlery działają poza blokadą:

- `PointsRealized` → `AnalyticsNotificationHandler`
- `WalletToppedUp` → `PushNotificationHandler`
- brak aktywności przez 4 okna → scheduler emituje `InactivityCheckTriggered`
  → handler sprawdza → `wallet.deactivate()`

---

### 11. Historia aktywności jako agregat write side

**Skąd wynika:** "raport aktywności grupuje realizacje w okna" — brzmi jak osobny byt
który trzeba zapisywać.

**Dlaczego źle:** grupowanie to reguła prezentacyjna. Żaden niezmiennik nie wymaga
blokowania historii razem z saldem.

**Poprawnie:** projekcja z dwóch źródeł:
- strumień eventów (`PointsRealized`, `WalletToppedUp`) — kiedy i ile,
- definicje okien (konfiguracja rytmu per portfel) — jak grupować.

Zero write-side agregatu. Raport budowany na żądanie z gotowych danych.

---

## Rozwiązanie docelowe

### Write side

```
LoyaltyWallet (agregat)
├── balance
├── availableBalance
├── windowRealizationCount
├── windowLimit
├── status
└── List<WalletAccess> (shares — wewnątrz, bo niezmiennik dostępu)

MemberProfile (CRUD encja)
├── memberId
├── numer członkowski + walidacja
├── imię, nazwisko + walidacja
├── data urodzenia + walidacja
├── email + walidacja
└── telefon + walidacja
```

### Pre-agregat (czyste funkcje)

```
BenefitPolicy.apply(requestedAmount, policyType) → finalAmount

PointsCalculator.calculate(PurchaseContext) → ComponentBreakdown
    ComponentBreakdown
    ├── Component(targetWalletId: buyer,    amount: X, calculator: tiered)
    ├── Component(targetWalletId: referrer, amount: Y, calculator: flat)
    └── Component(targetWalletId: operator, amount: Z, calculator: percentage)
```

### Serwis aplikacyjny — realizacja

```
1. BenefitPolicy.apply(requested, policy)  →  finalAmount
2. wallet.realize(finalAmount)             →  PointsRealized event
3. handler: AnalyticsNotificationHandler   →  system analityczny (eventual)
```

### Serwis aplikacyjny — doładowanie

```
1. PointsCalculator.calculate(context)     →  ComponentBreakdown
2. for each component in breakdown:
       walletRepository.findById(component.targetWalletId())
                       .topUp(component.amount())
3. handler: PushNotificationHandler        →  push do właściciela (eventual)
```

### Reset okna

```
Scheduler (zna rytm per portfel)  →  WindowResetCommand  →  wallet.resetWindow()
```

### Read side (projekcje)

```
ActivityReport       ←  PointsRealized eventy  +  definicje okien
MonthlySummary       ←  ActivityReport (grupowanie okien w miesiące)

```

---

### Taksonomia reguł

| Typ reguły | Przykład | Konstrukt |
|---|---|---|
| **Walidacyjna** | numer członkowski, nazwisko ≥ 2 znaki, email | CRUD encja + walidacja przy zapisie |
| **Spójnościowa** | dostępne saldo, licznik okna, dostępy, status | Agregat — jedyna granica blokowania |
| **Procesowa** | system analityczny, push, dezaktywacja po braku aktywności | Domain event + handler |
| **Obliczeniowa** | rabat polityki korzyści, naliczanie punktów z breakdownem | Czysta funkcja / pricing engine |
| **Prezentacyjna** | raport grupuje realizacje w okna, okna w miesiące | Read model / projekcja |
