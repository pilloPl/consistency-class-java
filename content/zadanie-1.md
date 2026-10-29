# Wymagania biznesowe

Sieć sklepów "Razem" chce, żeby klienci wracali — do jej sklepów i do uczestniczących sprzedawców. Za zakupy dostają punkty, które potem wydają na różne dobra. Program obejmuje klientów indywidualnych i firmy: firmy dają w nim benefit pracownikom, a sprzedawcy przyciągają do siebie klientów sieci.

## Kto jest kim

- **Operator programu** — firma, która prowadzi program dla sieci Razem i sprzedaje punkty firmom oraz sprzedawcom.
- **Uczestnik** — osoba z portfelem, która za zakupy zbiera punkty i wydaje je w sieci Razem; przy konkretnym zakupie nazywamy ją kupującym.
- **Osoba z dostępem** — korzysta z portfela uczestnika, np. członek rodziny.
- **Osoba polecająca** — uczestnik, który przyprowadził do programu innego uczestnika i dostaje punkty z jego zakupów.
- **Firma** — kupuje punkty od operatora i daje je pracownikom jako benefit.
- **Uczestniczący sprzedawca** — inna sieć (np. stacje paliw, kina), która przyznaje punkty swoim klientom; klienci wydają je potem w sklepach Razem, więc sprzedawca płaci za nie operatorowi z góry, a w zamian przyciąga do siebie klientów sieci bez budowania własnego programu.

## Uczestnictwo w programie

Przystąpienie do programu skutkuje natychmiastową aktywacją portfela lojalnościowego. Portfel **otrzymuje i przechowuje** unikalny numer w momencie otwarcia. Każdy uczestnik może mieć tylko jeden portfel.

Portfel **posiada dane właściciela**:

- numer członkowski (zgodny z zasadami walidacji — suma kontrolna),
- imię i nazwisko (nazwisko minimum 2 znaki),
- datę urodzenia (musi znajdować się w przeszłości),
- adres e-mail (poprawny format),
- numer telefonu (poprawny format krajowy).

Dane te można aktualizować w dowolnym momencie.

Portfel **przechowuje listę osób** z dostępem (np. członków rodziny, a w portfelach firmowych pracowników) — wszystkie osoby mogą korzystać z portfela.

---

## Saldo punktów

Portfel **trzyma saldo punktów** oraz **przechowuje informację** o tym, ile punktów jest dostępnych do realizacji. Aktywny portfel jest gotowy do realizacji punktów. Każde użycie też powinno być zapamiętane przez portfel (lista realizacji).

---

## Realizacja punktów

Uczestnik płaci punktami w aplikacji Razem — przy kasie w sklepach i punktach sprzedaży sieci albo w sklepie internetowym. Wskazuje, ile punktów chce wykorzystać. Realizacja jest możliwa, gdy portfel jest aktywny, a żądana liczba punktów nie przekracza dostępnego salda.

Portfel **ma przypisaną politykę korzyści**, którą **przechowuje**. Polityka określa, ile punktów faktycznie schodzi z salda. Niech $r$ oznacza liczbę punktów, które uczestnik realizuje, a $s$ liczbę punktów, które schodzą z salda. Różne polityki stosują różne algorytmy, np.:

- **Standard:** $s = r$,
- **Gold:** $s = \lceil 0{,}95 \cdot r \rceil$ (realizuje 100, ze salda schodzi 95),
- **Platinum:** $s = r - 10 \cdot \lfloor r / 100 \rfloor$, czyli za każde pełne 100 punktów 10 gratis (realizuje 250, ze salda schodzi 230).

Operator programu może zmienić politykę w dowolnym momencie. Nowa polityka obowiązuje od razu.

Po każdej realizacji zewnętrzny system analityczny sieci otrzymuje stosowną informację. Na podstawie tych danych dział finansowy rozlicza się z operatorem programu, dlatego dane w analityce muszą być zgodne z saldami portfeli. Rozliczenie jest okresowe, najczęściej miesięczne.

---

## Doładowanie portfela

Punkty trafiają do portfela w wyniku zakupów w sieci lub u uczestniczących sprzedawców. Doładowanie może nastąpić w dowolnym momencie. Nowe punkty są natychmiast dostępne do realizacji. Po doładowaniu uczestnik otrzymuje powiadomienie push z aktualnym saldem. Uczestnik nie może dostać powiadomienia o punktach, których nie ma w portfelu. Liczba punktów w powiadomieniu musi się zgadzać z faktycznym doładowaniem — sieć ma złe doświadczenia z takimi błędami.

Wyliczenie należnych punktów jest złożone — zależy od:

- wartości zakupu (inne przeliczniki dla różnych przedziałów kwotowych),
- poziomu uczestnika (Standard, Silver, Gold, Platinum),
- kanału transakcji (sklep stacjonarny, aplikacja mobilna, sklep internetowy),
- pory dnia (zakupy wieczorne 18:00–22:00 mają mnożnik bonusowy),
- aktywnych promocji przypisanych do produktu lub kategorii.

Wynik obliczeń jest rozkładany na wielu odbiorców według reguł powiązanych z daną transakcją. Zakup może skutkować doładowaniem portfela kupującego, osoby polecającej, operatora programu i innych uczestników — każdemu według osobnego algorytmu i innej kalkulacji. Liczba odbiorców z jednej transakcji nie jest z góry ustalona. Każdy odbiorca ma własny portfel, na który trafia jego porcja punktów.

Doładowanie może też się wydarzyć jako efekt reklamacji, zadośćuczynienia czy innych czynności operacyjnych.

---

## Okno realizacji

Portfel **ma tygodniowe okno realizacji** — maksymalną liczbę realizacji w okresie. Czyli mogę użyć portfela X razy w ciągu tygodnia. Portfel **przechowuje kolekcję okien realizacji** wraz z historią realizacji w każdym oknie. Większość uczestników ma okno **tygodniowe**, jednak niektórzy mają okno **miesięczne** — obowiązuje ono zamiast tygodniowego. Realizacja jest możliwa tylko gdy bieżące okno nie jest wyczerpane. W danej chwili aktywne jest jedno okno.

Portfel przechowuje pełną historię realizacji i doładowań. Portfel grupuje realizacje po oknach realizacji.

---

## Zamknięcie okna

Po upływie okresu okna generowany jest raport aktywności. Brak jakiejkolwiek aktywności przez 4 kolejne okna skutkuje dezaktywacją portfela.

---

## Razem Biznes

Firmy przystępują do programu przez Razem Biznes. Firma dostaje portfel firmowy i w razie potrzeby może otwierać kolejne, ale jednocześnie może prowadzić najwyżej trzy. Raz w miesiącu zasila je punktami kupionymi od operatora programu. Duże firmy zapisują do programu wszystkich pracowników.

Listę pracowników z dostępem do portfela firmowego prowadzi system kadrowy firmy. Razem Biznes pobiera z niego zmiany na bieżąco: nowy pracownik dostaje dostęp, a zwolniony go traci.

Razem prowadzi stołówki i automaty w biurowcach klientów biznesowych. Pracownicy płacą w nich punktami z portfela firmowego. Terminal pobiera punkty według cennika stołówki i drukuje potwierdzenie. Pobranie jest możliwe tylko z aktywnego portfela firmowego. Jeśli brakuje w nim punktów, pobranie się nie udaje i pracownik płaci normalnie. To, że jedni pracownicy wyczerpią pulę punktów innym, jest sprawą firmy. Program raportuje firmie, kto ile zużył.

Firma ustala w umowie, ile razy w oknie można użyć portfela firmowego. Zmiana umowy zmienia ten limit od następnego okna.

---

## Rozliczenia z uczestniczącymi sprzedawcami

Uczestniczący sprzedawcy (np. sieci stacji paliw, kina) kupują od operatora programu pakiety punktów z góry. Największe sieci mają setki punktów sprzedaży.

Punkty, które sprzedawca przyznaje swoim klientom za zakupy, są finansowane z jego pakietu. Sprzedawca nie może przyznać więcej punktów, niż ma opłacone w pakiecie. Za przekroczenie opłaconej puli sieć Razem zrywa ze sprzedawcą umowę. Pakiet można dokupić w dowolnym momencie. Z pakietu korzystają tylko aktywne kasy sprzedawcy zgłoszone do programu. Sprzedawca może w każdej chwili dezaktywować kasę. Zawieszony pakiet (np. po zerwaniu lub wygaśnięciu umowy ze sprzedawcą) nie finansuje punktów.

Każdy pakiet ma limit dzienny — maksymalną liczbę naliczeń w ciągu dnia, ustaloną w umowie ze sprzedawcą.

---

## Wymagania krytyczne

- realizacja punktów ponad dostępne saldo jest niedopuszczalna,
- doładowanie musi być natychmiast widoczne przy kolejnych realizacjach.

---

# Zadanie

**Nie modelujesz całego systemu.** Projektujesz tylko kawałek systemu, który pilnuje reguł spójnościowych.

Reguła spójnościowa to warunek, od którego zależy, czy operacja się uda, sprawdzany na danych, które w tej samej chwili może zmienić ktoś inny. Każda znaleziona reguła ma swoją konsekwencję: sprawdzenie warunku i zmiana danych muszą się odbyć w jednym kroku, tak żeby nikt inny nie zmienił tych danych pomiędzy.

**Liczba kawałków jest dowolna** — może to być jeden albo kilka. Dla każdego pokaż, jakie dane ma, jakie operacje przyjmuje i które reguły sprawdza przy każdej z nich.

Forma też jest dowolna: szkic na kartce, model, pseudokod, tabele lub dokumenty — coś, co da się zaimplementować w dowolnej technologii. Czego nie wiesz, dopytaj biznes.
