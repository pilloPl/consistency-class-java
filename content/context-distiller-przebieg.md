# context-distiller: przebieg i wynik

Program lojalnościowy „Razem”, sesja z 28.10.2026. Wywołanie po pytaniu „a potrafisz mi rozstrzygnąć context-distillerem, czy to jest to samo, czy nie?”. Tryb sondy jednego pojęcia: portfel uczestnika / portfel firmowy / pakiet sprzedawcy.

## Analiza przed pytaniami

Działam w trybie sondy pojedynczego pojęcia. Sprawdzam, czy portfel uczestnika, portfel firmowy i pakiet sprzedawcy robią w jakimś kontekście to samo.

### Krok 1: czasowniki na trzech rzeczownikach

| Czasownik | Portfel uczestnika | Portfel firmowy | Pakiet sprzedawcy |
|---|---|---|---|
| **obciążenie** | realizacja (s liczone z r przez politykę) | pobranie (kwota z cennika stołówki) | sfinansowanie, czyli naliczenie punktów klientowi |
| warunki obciążenia | aktywny, s ≤ saldo, okno niewyczerpane | aktywny, kwota ≤ saldo, limit z umowy w oknie | niezawieszony, punkty ≤ opłacone, limit dzienny, aktywna kasa |
| **zasilenie** | doładowanie (zakup, polecenie, reklamacja) + push | zasilenie raz w miesiącu punktami kupionymi od operatora | kupno pakietu z góry, dokupienie „w dowolnym momencie” |
| kto obciąża | właściciel + osoby z dostępem | pracownicy z systemu kadrowego | aktywne, zgłoszone kasy (setki) |
| okno | tydzień albo miesiąc; limit + grupowanie historii + miara aktywności | limit z umowy, zmiana od następnego okna | dzień, limit z umowy |
| blokada | dezaktywacja po 4 pustych oknach | „aktywny portfel firmowy”, przyczyny nie podano | zawieszenie po zerwaniu albo wygaśnięciu umowy |
| skutek odmowy | brak realizacji | „pracownik płaci normalnie” | brak naliczenia; w umowie zerwanie za przekroczenie |
| tylko tu | dane właściciela, polityka korzyści, push, analityka, jeden na osobę | maks. 3 na firmę, synchronizacja z kadrami, raport „kto ile” | dezaktywacja kasy, „pakiety” w liczbie mnogiej |

### Krok 2: analiza dwukierunkowa

**A. Jedno słowo, kilka znaczeń (kandydaci do podziału)**

| Słowo | Znaczenie 1 | Znaczenie 2 | Wstępne rozstrzygnięcie |
|---|---|---|---|
| Portfel | pula punktów, z której się płaci | tożsamość uczestnika (dane, polityka, jeden na osobę) albo benefit firmy (maks. 3, kadry) | podział: „pula” osobno, „członkostwo” i „Razem Biznes” osobno |
| Okno | limit częstotliwości obciążeń (wszystkie trzy) | okres raportu i miara aktywności, po której następuje dezaktywacja (tylko portfel uczestnika?) | podział: okres limitu ≠ okres aktywności |
| Aktywny / zawieszony | brak aktywności przez 4 okna | zerwana albo wygasła umowa | przyczyny są różne, skutek może być ten sam (pytanie 1) |
| Realizacja | obciążenie puli | zdarzenie, na podstawie którego finanse rozliczają się z operatorem | analityka to konsekwencja w procesie, nie część puli |

**B. Kilka słów, jedno znaczenie (kandydaci do uogólnienia)**

| Słowa | Wspólne zachowanie | Uogólnienie |
|---|---|---|
| realizacja / pobranie / sfinansowanie | „czy mogę zdjąć N punktów teraz?”: aktywny ∧ N ≤ saldo ∧ licznik okna < limit; potem saldo −N, licznik +1 | **Obciążenie** |
| doładowanie / zasilenie / dokupienie | saldo +N, od razu dostępne | **Zasilenie** |
| dezaktywacja / zawieszenie | po nich nie da się obciążyć | **Blokada obciążeń** (bez przyczyny) |
| osoby z dostępem / pracownicy / kasy | „czy ten aktor może obciążyć tę pulę?” | **Uprawniony** (nieprzezroczyste id) |
| polityka korzyści / cennik stołówki / kalkulator naliczeń | wyliczają N, zanim padnie obciążenie | zostają **poza** pulą, pula dostaje gotowe N |

Jedna różnica wyszła na poziomie przepływu, nie salda. Realizacja i pobranie wyprowadzają punkty z obiegu, w zamian za towar albo obiad. Sfinansowanie to przeniesienie punktów z pakietu do portfela klienta. Dla samej puli w obu przypadkach to obciążenie. Na poziomie przepływu punktów to przelewy do różnych kont.

**C. Proponowane dodatkowe pojęcia (spekulatywne)**

| Uogólnienie | Pojęcie | Dlaczego pasuje |
|---|---|---|
| Pula | konto operatora programu | „zakup może doładować portfel operatora”, więc jest odbiorcą zasilenia |
| Pula | konto emisji sieci Razem | skądś muszą pochodzić punkty za zakupy w samej sieci Razem |
| Pula | konto rozliczeniowe realizacji | trafiają tam zrealizowane punkty; na nim dział finansowy rozlicza się z operatorem |
| Pula | karta podarunkowa / bon punktowy | zasilenie z góry, obciążenia, brak właściciela-uczestnika |

## Pytania skilla i odpowiedzi

### Pytanie 1: Blokada

Test skutku: portfel uczestnika nieaktywny po 4 pustych oknach, portfel firmowy nieaktywny, pakiet zawieszony po zerwaniu umowy. Czy skutek jest wszędzie ten sam?

- **Blokuje tylko obciążenia**: Nie da się zdjąć punktów, ale da się doładować lub dokupić. Pula zna tylko flagę, a przyczyna zostaje w kontekście źródłowym.
- ✅ **Blokuje wszystko**: Ani obciążenia, ani zasilenia. Nadal ten sam skutek, więc uogólnienie się trzyma.
- **Różnie per typ**: Np. zawieszony pakiet można dokupić, a do nieaktywnego portfela uczestnika nic nie wpada. Wtedy pula musi znać typ, czyli granica przebiega gdzie indziej.
- **To zależy**: Dopowiem.

**Odpowiedź:** Blokuje wszystko

### Pytanie 2: Ile pakietów

„Kupują pakiety”, „każdy pakiet ma limit dzienny”. Czy sprzedawca ma naraz kilka pakietów i system wybiera, który finansuje naliczenie?

- ✅ **Jeden, dokupienie zasila**: Jedna pula na sprzedawcę. Dokupienie zwiększa saldo, tak samo jak doładowanie portfela.
- **Wiele, z wyborem**: Np. najpierw najstarszy albo ten, który wcześniej wygasa. Wybór puli to proces specyficzny dla sprzedawcy, lecz każdy pakiet nadal jest pulą.
- **Wiele, bez wyboru**: Każdy pakiet jest przypisany do konkretnych kas albo umowy, więc nie ma wyboru.
- **To zależy**: Dopowiem.

**Odpowiedź:** Jeden, dokupienie zasila

### Pytanie 3: Aktywność

Raport aktywności po zamknięciu okna i dezaktywacja po 4 pustych oknach: kogo dotyczą?

- ✅ **Tylko portfela uczestnika**: To proces członkostwa. Okno w puli służy tylko do limitu, a aktywność to osobny kontekst, który wysyła „zablokuj”.
- **Wszystkich trzech**: Pakiet i portfel firmowy też wygasają z braku użycia. Okno aktywności byłoby wtedy wspólne.
- **To zależy**: Dopowiem.

**Odpowiedź:** Tylko portfela uczestnika

### Pytanie 4: Uprawnieni

Uprawnienie do obciążenia (osoba z dostępem, pracownik z kadr, aktywna kasa): czy sprawdzenie musi być atomowe z obciążeniem, czy wystarczy odpowiedź „uprawniony tak/nie” z kontekstu źródłowego?

- ✅ **Wystarczy tak/nie**: Kasa dezaktywowana sekundę wcześniej albo pracownik zwolniony przed chwilą to nie problem. Lista uprawnionych żyje poza pulą.
- **Atomowo z obciążeniem**: Lista uprawnionych musi być w puli. Przy setkach kas i tysiącach pracowników to robi się ciężkie.
- **Różnie per typ**: Np. kasy atomowo, osoby nie.
- **To zależy**: Dopowiem.

**Odpowiedź:** Wystarczy tak/nie

## Wynik

## Destylacja kontekstów: program „Razem”, pule punktów

**Werdykt: w kontekście obciążeń i zasileń to jest to samo pojęcie.** Portfel uczestnika, portfel firmowy i pakiet sprzedawcy dają się uogólnić do **Konta punktowego**. Realizacja, pobranie i sfinansowanie to jedno **Obciążenie**. Doładowanie, zasilenie i dokupienie to jedno **Zasilenie**. Poza tym kontekstem te trzy pojęcia się rozchodzą: każde ma własne procesy (członkostwo i aktywność, Razem Biznes, umowy ze sprzedawcami).

Każda z czterech odpowiedzi potwierdziła uogólnienie. Skutek blokady jest wszędzie ten sam. Jest jeden pakiet na sprzedawcę, a dokupienie zasila jego saldo. Aktywność dotyczy tylko uczestnika. Uprawnienie wystarczy w postaci tak/nie.

### Podsumowanie analizy językowej

#### Niejednoznaczności (jedno słowo, kilka znaczeń)

| Słowo | Kontekst A | Znaczenie A | Kontekst B | Znaczenie B | Rozstrzygnięcie |
|---|---|---|---|---|---|
| Portfel | Konto punktowe | pula z saldem, limitem i blokadą | Członkostwo / Razem Biznes | tożsamość uczestnika (dane, polityka, jeden na osobę) albo benefit firmy (maks. 3, kadry) | podział |
| Okno | Konto punktowe | okres limitu obciążeń (dzień, tydzień, miesiąc) | Aktywność uczestnika | okres raportu i miary aktywności | podział: aktywność tylko u uczestnika (A) |
| Nieaktywny / zawieszony | Konto punktowe | zablokowane: ani obciążeń, ani zasileń | Członkostwo / Sprzedawcy | 4 puste okna / zerwana albo wygasła umowa | przyczyna zostaje w źródle, do konta trafia tylko skutek (A) |
| Realizacja | Konto punktowe | obciążenie | Rozliczenia z operatorem | zdarzenie, na którym finanse się rozliczają | analityka to konsekwencja w procesie |

#### Uogólnienia (kilka słów, jedno znaczenie)

| Słowa | Kontekst | Wspólne zachowanie | Uogólnione jako | Technika |
|---|---|---|---|---|
| portfel uczestnika, portfel firmowy, pakiet sprzedawcy | Konto punktowe | saldo, limit w oknie, blokada | **Konto punktowe** | wyabstrahowanie |
| realizacja, pobranie, sfinansowanie (naliczenie) | Konto punktowe | aktywne ∧ N ≤ saldo ∧ licznik < limit, potem saldo −N i licznik +1 | **Obciążenie** | uogólnienie (bez skąd N i po co) |
| doładowanie, zasilenie miesięczne, dokupienie pakietu | Konto punktowe | saldo +N, od razu dostępne | **Zasilenie** | uogólnienie (bez źródła) |
| dezaktywacja, zawieszenie | Konto punktowe | blokuje obciążenia i zasilenia | **Blokada** | uogólnienie (bez przyczyny) |
| osoba z dostępem, pracownik, kasa | Konto punktowe | aktor operacji zapisany w księdze | **aktorId** (nieprzezroczysty) | uogólnienie (lista uprawnionych poza kontem) |
| polityka korzyści, cennik stołówki, kalkulator naliczeń | konteksty źródłowe | wyliczają N przed obciążeniem lub zasileniem | nie wchodzą do konta | zmiana reprezentacji |
| tygodniowe, miesięczne, dzienne okno | Konto punktowe | licznik obciążeń w okresie | **okno limitu** (parametr: długość) | uogólnienie |

#### Proponowane dodatkowe pojęcia (spekulatywne)

| Uogólnienie | Pojęcie | Dlaczego pasuje | Status |
|---|---|---|---|
| Konto punktowe | konto operatora programu | jest w wymaganiach jako odbiorca porcji z zakupu | spekulatywne; trzeba potwierdzić, czy ma limit i blokadę |
| Konto punktowe | konto emisji sieci Razem | źródło punktów za zakupy w sklepach Razem: obciążenie emisji = zasilenie klienta | spekulatywne |
| Konto punktowe | konto rozliczeniowe realizacji | dokąd trafiają zrealizowane punkty; to na nim rozlicza się dział finansowy | spekulatywne |
| Konto punktowe | karta podarunkowa / bon punktowy | zasilenie z góry, obciążenia, blokada, bez właściciela-uczestnika | spekulatywne |

### Mapa kontekstów

#### Konto punktowe (uogólnione)

**Kluczowe pytanie**: „Czy z konta K można teraz zdjąć N punktów?”

| Pojęcia źródłowe | Uogólnione jako | Co zostaje | Co odpada |
|---|---|---|---|
| portfel uczestnika, portfel firmowy, pakiet | Konto | kontoId, saldo, status (aktywne / zablokowane), okno limitu (długość, limit, licznik, początek), limit na następne okno, księga operacji | dane właściciela, polityka korzyści, umowa, cennik, lista uprawnionych, przyczyna blokady, typ konta, push, analityka |
| realizacja, pobranie, sfinansowanie | Obciążenie | N, aktorId, referencja | r i polityka, pozycja z cennika, klient sprzedawcy |
| doładowanie, zasilenie, dokupienie | Zasilenie | N, referencja | źródło (zakup, polecenie, reklamacja, opłata od firmy lub sprzedawcy) |

**Polecenia**: `obciąż(konto, N, aktor, ref)`, `zasil(konto, N, ref)`, `zablokuj(konto, oczekiwanaWersja?)`, `ustawLimit(konto, limit)` (od następnego okna)
**Zdarzenia**: `Obciążono{konto, N, aktor, ref, saldoPo}`, `ObciążenieOdrzucone{saldo | limit | blokada}`, `Zasilono{konto, N, ref, saldoPo}`, `Zablokowano`, `OknoZamknięte{konto, licznik}`

**Czego ten kontekst NIE wie:**
- czyje to konto (uczestnika, firmy, sprzedawcy) i dlaczego jest zablokowane,
- jak policzono N (polityka, cennik, kalkulator),
- kto ma prawo obciążać (dostaje gotowe tak/nie i aktorId tylko do księgi),
- co się dzieje po odmowie (odmowa, płatność normalna, naruszenie umowy).

---

#### Członkostwo uczestnika (specyficzny)

**Kluczowe pytanie**: „Kim jest uczestnik i na jakich warunkach korzysta z programu?”
**Pojęcia**: Uczestnik, numer członkowski, dane właściciela (CRUD), osoby z dostępem, poziom, polityka korzyści.
**Procesy specyficzne**: przystąpienie (zakłada Konto), „jeden portfel na uczestnika”, zmiana polityki przez operatora.
**Integracja**: przelicza r na s polityką i wywołuje `obciąż(konto, s)`. Odpowiada tak/nie na pytanie o uprawnienie osoby.

#### Aktywność uczestnika (specyficzny)

**Kluczowe pytanie**: „Czy uczestnik był aktywny w ostatnich 4 oknach?”
**Procesy specyficzne**: raport aktywności po zamknięciu okna, dezaktywacja (tylko uczestnicy, A).
**Integracja**: konsumuje `Obciążono`, `Zasilono` i `OknoZamknięte`. Wysyła `zablokuj(konto, oczekiwanaWersja)`. Wersja zamyka wyścig: jeśli od decyzji wpadło doładowanie, blokada nie przejdzie, a konto nie musi znać reguły „4 puste okna”.

#### Razem Biznes (specyficzny)

**Kluczowe pytanie**: „Które konta prowadzi firma, kto z nich korzysta i na jakich warunkach?”
**Pojęcia**: Firma, umowa (limit użyć), maks. 3 konta, pracownicy z kadr, cennik stołówki.
**Procesy specyficzne**: synchronizacja z kadrami, comiesięczne zasilenie (płatność firmy, potem `zasil`), zmiana umowy (`ustawLimit`), wydruk potwierdzenia, raport „kto ile” (projekcja po `Obciążono.aktor`), odmowa skutkuje płatnością normalną.

#### Uczestniczący sprzedawcy (specyficzny)

**Kluczowe pytanie**: „Czy sprzedawca ma ważną umowę i które jego kasy uczestniczą?”
**Pojęcia**: Sprzedawca, umowa (limit dzienny), rejestr kas, jeden pakiet = jedno Konto (A).
**Procesy specyficzne**: zgłoszenie i dezaktywacja kasy, zerwanie albo wygaśnięcie umowy (`zablokuj`), dokupienie (płatność, potem `zasil`). Klauzula „zerwanie za przekroczenie” przy twardym limicie zostaje zapisem umowy, system do niej nie dopuszcza.

#### Przepływ punktów (integracyjny)

**Kluczowe pytanie**: „Skąd i dokąd przechodzą punkty z danej transakcji?”
**Procesy**: sfinansowanie to `obciąż(pakiet)` i potem `zasil(portfel klienta)`. Rozkład zakupu na odbiorców jest niezależny dla każdego portfela. Do tego push z `Zasilono.saldoPo` i analityka z `Obciążono`.

### Uwagi o bezpieczeństwie uogólnienia

**Granice, które mogą się przesunąć:**
- **Gorące konta.** Portfel firmowy w porze lunchu i pakiet dużej sieci obsługują wiele równoległych obciążeń. To kusi, żeby rozbić model per typ. Rozbijaj jednostkę spójności (np. podkonta per kasa lub stołówka), nie pojęcie.
- **Blokada blokuje też zasilenia (A).** Sfinansowanie dla klienta z zablokowanym portfelem kończy się więc obciążeniem pakietu bez zasilenia. Przepływ potrzebuje wtedy kompensacji (zwrotu do pakietu) albo sprawdzenia przed obciążeniem. Tego wymagania nie rozstrzygają.

**Uogólnienie trzeba wznowić, jeśli:**
- jeden typ dostanie regułę, która wymaga wiedzy o typie wewnątrz konta: wygasanie punktów tylko w pakiecie, debet dla firm, limit kwotowy zamiast liczbowego tylko u sprzedawcy,
- uprawnienie przestanie wystarczać w postaci tak/nie, np. kasy trzeba będzie sprawdzać atomowo; wtedy lista uprawnionych wchodzi do konta tylko dla jednego typu,
- sprzedawca dostanie kilka pakietów naraz z regułą wyboru (dziś: jeden, A).

### Sprawdzenie źródeł decyzji

| Decyzja | Źródło |
|---|---|
| obciążenie: aktywne ∧ N ≤ saldo ∧ licznik < limit | R + A (twardy limit, z poprzedniej rundy) |
| okno liczy obciążenia (realizacje, naliczenia) | R |
| blokada: ten sam skutek dla trzech typów | A |
| jeden pakiet, dokupienie = zasilenie | A |
| aktywność i dezaktywacja tylko u uczestnika | A |
| uprawnienie poza kontem | A |
| polityka, cennik i kalkulator poza kontem | L + zasada 6 |
| limit z umowy obowiązuje od następnego okna także dla uczestnika i sprzedawcy | X: założenie o niskim wpływie, parametr `ustawLimit` działa tak samo |
| analityka po każdym obciążeniu, nie tylko po realizacji | X: konsekwencja w procesie, granicy nie przesuwa |

### Notatki
- Konto punktowe z kontami operatora, emisji i rozliczeniowym to wprost **archetyp księgowy**: każda operacja jest przelewem między kontami, a realizacja i sfinansowanie różnią się tylko kontem docelowym. Następny krok: `accounting-archetype-mapper` na kontekście Konta punktowego.
- Potem `aggregate-designer` dla Konta: które polecenia blokują się wzajemnie i jak dzielić gorące konta.
- To rozstrzygnięcie zmienia wcześniejszą klasyfikację. Zamiast trzech osobnych komponentów RC jest jeden: Konto punktowe.
