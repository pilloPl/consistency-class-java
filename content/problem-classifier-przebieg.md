# problem-classifier: przebieg i wynik

Program lojalnościowy „Razem”, sesja z 28.10.2026. Wejście: wymagania biznesowe z `zadanie-v2.md`.

## Pytania skilla i odpowiedzi

### Pytanie 1: Pakiet

Pakiet sprzedawcy: „nie może przyznać więcej, niż ma opłacone”, ale też „za przekroczenie puli zrywamy umowę”. Co ma się stać, gdy setki kas jednocześnie naliczają punkty, a w pakiecie zostało mało?

- ✅ **Twarda blokada**: Nasz system odrzuca naliczenie ponad pulę. Sprawdzenie i obciążenie pakietu muszą być atomowe, więc to Resource Contention.
- **Wykrycie po fakcie**: Naliczenia przechodzą, a przekroczenie wykrywamy potem i zrywamy umowę. Wtedy nie trzeba agregatu, wystarczy licznik i reakcja (Integration/T&P).
- **Blokada + zerwanie**: Blokujemy, a samą próbę przekroczenia traktujemy jako naruszenie umowy (zdarzenie biznesowe).

**Odpowiedź:** Twarda blokada

### Pytanie 2: Odbiorcy

Jeden zakup doładowuje kilka portfeli (kupujący, polecający, operator, inni). Czy wszyscy muszą dostać punkty w jednej transakcji?

- ✅ **Niezależnie**: Każdy portfel doładowujemy osobno. Liczy się gwarancja, że każdy w końcu dostanie swoją porcję, nawet z kilkusekundowym opóźnieniem.
- **Wszystko albo nic**: Kupujący nie może mieć punktów, dopóki nie dostanie ich polecający. Potrzebna saga albo kompensacja.

**Odpowiedź:** Niezależnie

### Pytanie 3: Limity

Limit użyć w oknie (portfel prywatny, portfel firmowy przy stołówce) oraz dzienny limit naliczeń pakietu: jak twardy jest ten limit przy równoległych użyciach?

- ✅ **Twardy, z saldem**: Ani jednego użycia ponad limit. Licznik okna sprawdzamy atomowo razem z saldem i statusem.
- **Miękki**: Lekkie przekroczenie przy wyścigu jest akceptowalne. Limit można sprawdzić osobno, poza jednostką spójności salda.
- **Różnie**: Np. w portfelu prywatnym twardy, a w firmowym i w pakiecie miękki.

**Odpowiedź:** Twardy, z saldem

### Pytanie 4: Analityka

Analityka sieci musi zgadzać się z saldami, a rozliczenie jest miesięczne. Czy wystarczy, że informacja o realizacji dotrze z opóźnieniem, byle nie zginęła?

- ✅ **Opóźnienie OK**: Gwarantowane dostarczenie (outbox), spójność ostateczna, nic nie ginie i nic się nie dubluje.
- **Musi być od razu**: Realizacja nie może się udać, dopóki analityka jej nie przyjmie. Synchroniczne wywołanie, które blokuje realizację.

**Odpowiedź:** Opóźnienie OK

## Wynik

### Klasyfikacja: złożona. Rdzeń to Resource Contention, obudowany Integration i T&P

**Pewność**: wysoka. Wymaganie łączy kilka klas, więc dzielę je na komponenty niżej.

#### Ścieżka dedukcji

| # | Pytanie | Odpowiedź | Sygnał / wniosek |
|---|---------|-----------|------------------|
| 1 | Czy jedynym skutkiem zmiany danych właściciela (imię, e-mail, telefon…) jest to, co widać na ekranie? | z wymagań: „można aktualizować w dowolnym momencie”, nic od nich nie zależy | CRUD z mocną walidacją. Wszystkie sprawdzane wartości pochodzą z żądania |
| 2 | Czy dane sprawdzane przy realizacji (status, saldo, licznik okna) może w tej samej chwili zmienić inne polecenie? | z wymagań: osoby z dostępem korzystają z jednego portfela, doładowanie „w dowolnym momencie”, pracownicy przy stołówce | **RC.** Wyścig na saldzie i liczniku |
| 3 | Jak twardy jest limit użyć w oknie i dzienny limit naliczeń? | **Twardy, sprawdzany razem z saldem** | Licznik okna należy do tej samej jednostki spójności co saldo i status |
| 4 | Pakiet sprzedawcy: blokujemy czy wykrywamy przekroczenie po fakcie? | **Twarda blokada** | Pakiet to RC, nie licznik z reakcją. Ten system sam odpowiada na pytanie „czy mogę przyznać?” |
| 5 | Czy wszyscy odbiorcy z jednego zakupu muszą dostać punkty w jednej transakcji? | **Niezależnie**, z gwarancją dostarczenia | Integration przez choreografię i outbox, bez sagi z kompensacją. Każdy portfel to osobna jednostka spójności |
| 6 | Czy analityka może dostać realizację z opóźnieniem? | **Tak**, byle nic nie zginęło | Outbox w transakcji realizacji, idempotentny odbiorca, miesięczna rekonsyliacja |
| 7 | Czy wyliczenie należnych punktów zmienia stan? | z wymagań: wejście to zakup, wyjście to lista (odbiorca, ile) | T&P. Czysta funkcja, poza agregatem |
| 8 | Czy polityka korzyści (Standard/Gold/Platinum) coś blokuje? | z wymagań: tylko wylicza s z r | T&P (kalkulator). Zmiana przypisanej polityki to CRUD. Niezmiennik salda sprawdzamy na już wyliczonym s |
| 9 | Czy historia realizacji pogrupowana po oknach decyduje o czymkolwiek? | z wymagań: „przechowuje”, „grupuje” | T&P. Do decyzji wystarczy licznik bieżącego okna, nie cała historia |

#### Dlaczego tak
- „realizacja ponad dostępne saldo jest niedopuszczalna” + wiele osób z dostępem do jednego portfela → klasyczny wyścig check-then-write
- „Realizacja jest możliwa tylko gdy bieżące okno nie jest wyczerpane” + odpowiedź „twardy” → licznik okna w tej samej transakcji co saldo
- „Sprzedawca nie może przyznać więcej punktów, niż ma opłacone” + „setki punktów sprzedaży” + „limit dzienny” → drugi gorący zasób
- „zewnętrzny system analityczny… otrzymuje”, „powiadomienie push”, „system kadrowy… na bieżąco” → trzy granice integracyjne
- „Wyliczenie należnych punktów jest złożone” → polityka liczona poza agregatem (pułapka „policy says how many”)

#### Proponowany podział

| Komponent | Klasa | Uzasadnienie |
|-----------|-------|--------------|
| Dane właściciela portfela | CRUD | Walidacja sumy kontrolnej, formatów i daty dotyczy tylko wejścia. Nic nie blokuje i niczego nie odblokowuje |
| Otwarcie portfela, unikalny numer, jeden portfel na uczestnika | CRUD + `UNIQUE` | „Tylko jeden” rozwiązuje constraint w bazie. Agregat niepotrzebny |
| Lista osób z dostępem (rodzina) | CRUD | Decyduje o autoryzacji, nie o puli |
| Przypisanie polityki korzyści (operator) | CRUD | Zmiana parametru. Wchodzi „od razu”, przy najbliższym poleceniu |
| Kalkulator polityki (r → s) | T&P | Czysta funkcja: Standard, Gold, Platinum |
| **Realizacja punktów** | **RC** | Status aktywny + saldo ≥ s + licznik okna < X, sprawdzane atomowo |
| **Doładowanie portfela** | **RC** (ta sama jednostka) | Zmienia to samo saldo co realizacja (ryzyko lost update) i musi być „natychmiast widoczne”, więc saldo czytamy ze źródła prawdy, nie z cache |
| Wyliczenie należnych punktów i rozkład na odbiorców | T&P | Pipeline: przedział kwoty, poziom, kanał, pora dnia, promocje. Wynik to `[(portfel, ile)]` |
| Rozesłanie porcji do portfeli odbiorców | Integration | Choreografia: osobne doładowanie na każdy portfel, idempotentne po `(transakcja, odbiorca)` |
| Push po doładowaniu | Integration | Z outboxa po commicie. Kwota i saldo pochodzą ze zdarzenia, nie z ponownego odczytu |
| Realizacja → analityka | Integration | Outbox w transakcji realizacji, idempotencja po id realizacji, rekonsyliacja przed rozliczeniem |
| Historia pogrupowana po oknach, raport aktywności, raport „kto ile zużył” | T&P | Projekcje z księgi operacji |
| Dezaktywacja po 4 pustych oknach | RC (status w tej samej jednostce) + job | Job wysyła polecenie, a portfel przy zapisie sprawdza ponownie „brak aktywności”. Inaczej doładowanie, które wpadnie w tej samej chwili, trafi do portfela właśnie dezaktywowanego |
| Maks. 3 portfele firmowe naraz | CRUD + prosty warunek (do potwierdzenia) | Rzadka operacja jednego administratora. Agregat Firma tylko wtedy, gdy realnie otwiera się je równolegle |
| Synchronizacja z systemem kadrowym | Integration | Kolejność zdarzeń (zatrudnienie i zwolnienie tej samej osoby), idempotencja |
| **Pobranie na stołówce (portfel firmowy)** | **RC** | Ten sam kształt co realizacja, z limitem z umowy |
| Umowa firmy, limit „od następnego okna” | CRUD | Portfel zapamiętuje limit przy otwarciu okna, więc zmiana umowy nie ściga się z bieżącym oknem |
| **Sfinansowanie punktów z pakietu sprzedawcy** | **RC** | Pakiet aktywny + saldo pakietu ≥ punkty + dzienny licznik < limit. Twarda blokada |
| Dokupienie pakietu | RC (ta sama jednostka co pakiet) | Zmienia saldo pakietu równolegle z naliczeniami |
| Rejestr kas sprzedawcy (aktywacja i dezaktywacja) | CRUD (do potwierdzenia) | Zakładam, że nie trzeba go sprawdzać atomowo z pakietem (pytanie otwarte niżej) |
| Pakiet → portfel klienta sprzedawcy | Integration | Najpierw obciążenie pakietu (to on pilnuje limitu), potem gwarantowane doładowanie portfela |

#### Diagram komponentów

```
                     [Kalkulator należnych punktów / T&P]
                                    | [(odbiorca, ile)]
  kasa sprzedawcy                   v
  ---------------> [Pakiet sprzedawcy / RC] --PunktySfinansowane--+
                                                                   |
  kasa Razem / app ------------------------------------------------+
                                                                   v
                                                 (outbox, per odbiorca, idempotentnie)
                                                                   v
  [Kalkulator polityki r→s / T&P] --s--> [Portfel / RC] <-- doładowanie (zakup, reklamacja, firma co miesiąc)
                                       saldo + status + licznik okna
  [Dane właściciela / CRUD]            :  (jedna jednostka spójności)
  [Osoby z dostępem / CRUD] <-sync-- [System kadrowy] (Integration)
                                       |
             +-------------------------+-----------------------+
             v                         v                       v
   ZrealizowanoPunkty           DoładowanoPortfel       OknoZamknięte
             |                         |                       |
   [Analityka / Integration]    [Push / Integration]    [Raport aktywności / T&P]
                                                        [Job dezaktywacji -> polecenie do Portfela]
  [Historia wg okien / T&P] <---- księga operacji portfela
```

#### Czego nie robić
- **Nie ładuj pełnej historii realizacji i doładowań do agregatu.** „Portfel przechowuje kolekcję okien wraz z historią” to opis widoku, nie jednostki spójności. Do decyzji wystarczą saldo, status, licznik bieżącego okna i jego limit.
- **Nie wkładaj kalkulatora punktów (5 wymiarów) ani polityki korzyści do agregatu.** Agregat dostaje gotowe s albo gotową kwotę doładowania.
- **Nie licz salda dla realizacji z read modelu ani z cache.** Wymaganie „natychmiast widoczne” to wyklucza.
- **Nie przepuszczaj danych właściciela przez agregat portfela**, mimo że wymagania mówią „portfel posiada dane właściciela”. To ukryty CRUD.
- **Nie rób sagi „wszystko albo nic” dla odbiorców.** Odpowiedź brzmiała: niezależnie.

#### Sugerowane podejście
1. **Jedna jednostka spójności na pulę.** Saldo, status i licznik okna sprawdzasz i zapisujesz razem, z optimistic lockingiem. Doładowanie przechodzi przez tę samą jednostkę, bo zmienia to samo saldo.
2. **Realizacja, pobranie na stołówce i sfinansowanie z pakietu mają ten sam kształt:** pula punktów, limit użyć w oknie (tydzień, miesiąc albo dzień), status aktywności i lista uprawnionych (osoby albo kasy). Warto sprawdzić, czy to jeden model z różnymi parametrami. Różnice są w konsekwencjach: wyczerpanie puli firmowej „to sprawa firmy”, a pakiet ma twardą granicę umowną.
3. **Każde zdarzenie wychodzące (analityka, push, porcje dla odbiorców) zapisuj do outboxa w tej samej transakcji co zmianę salda.** Wtedy „nie powiadamiamy o punktach, których nie ma” i „analityka zgodna z saldami” wynikają z konstrukcji, a nie z dyscypliny.

#### Pytania otwarte przed modelowaniem
- **Gorące jednostki.** Portfel firmowy w porze lunchu i pakiet dużej sieci przy setkach kas to wiele równoległych poleceń na jednym agregacie. Optimistic locking da tam lawinę konfliktów. Czy da się podzielić pulę albo limit (np. przydział per kasa lub per stołówka), czy biznes wymaga jednej, globalnej puli?
- **Dezaktywacja kasy a naliczenie w tej samej chwili.** Czy naliczenie z kasy, którą sprzedawca właśnie dezaktywuje, to problem? Jeśli tak, status kasy trafia do jednostki spójności pakietu.
- **Usunięcie pracownika z dostępu a pobranie w tej samej chwili.** Jak wyżej: czy wystarczy autoryzacja „sprzed chwili”?
- **Zrywanie umowy za przekroczenie puli.** Przy twardej blokadzie przekroczenie nie powinno się zdarzyć. Czy odrzucona próba jest zdarzeniem biznesowym, np. sygnałem naruszenia umowy, czy tylko odmową?
- **Doładowanie nieaktywnego portfela.** Odrzucamy, wstrzymujemy czy reaktywujemy?
- **„Aktualne saldo” w pushu.** Saldo zaraz po tym doładowaniu (ze zdarzenia), czy w chwili wysyłki? Przy równoległej realizacji te wartości się różnią.
- **Maks. 3 portfele firmowe.** Czy kilka osób w firmie może je otwierać równolegle?
- **Czy pakiet ma ten sam kształt co portfel?** Pakiet ma limit dzienny liczony w naliczeniach, a portfel limit liczony w realizacjach. Trzeba sprawdzić, czy to faktycznie ten sam kształt (krok aggregate-designer / context-distiller).

