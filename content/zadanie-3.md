# Zadanie: model w kodzie

Granice są już ustalone. Zaczynamy od wspólnego stanu z omówienia zadania 1: **portfel**.

**Wariant A: portfel z nadaniem i odebraniem dostępu**

<div class="diagram" data-diagram="wariantA"></div>

**Wariant B: portfel bez dostępu**

<div class="diagram" data-diagram="wariantB"></div>

Na tych granicach zaprojektuj konkretny model w kodzie. Napisz testy i kod. Testy mają pokazać, że każda reguła się trzyma, także wtedy, gdy dwie operacje trafią w ten sam portfel w tej samej chwili.

---

## Zanim zaczniesz pisać, odpowiedz sobie na pytania

1. **Kolejność w procesie.** Czy biznes rozdziela w czasie którąś parę operacji, np. otwarcie nowego okna i zużycie punktów? Jeśli tak, czy ta para w ogóle musi się blokować?
2. **Obciążenie.** Ile operacji trafia w jeden portfel naraz w szczycie: obiad w stołówce, setki kas na jednym pakiecie? Czy szczyty różnych operacji się nakładają? Czy operacje na dwóch różnych portfelach kiedykolwiek ze sobą rywalizują?
3. **Najmniej danych.** Jakich danych potrzebuje każda operacja, żeby zdecydować, czy się uda? Gdzie wystarczy licznik albo sam identyfikator zamiast całej historii?
4. **Blokowanie.** Optymistycznie (wersja i ponowienie) czy pesymistycznie (czekanie na blokadę)? Co się stanie, gdy w jeden portfel uderzy naraz sto pobrań? Które reguły muszą się trzymać natychmiast, a które mogą się dopiąć chwilę później?

---

## Decyzje, które pokazujesz

Przy każdej odpowiedz **dlaczego**:

- **Styl.** Piszesz kod obiektowy, funkcyjny czy bazodanowy, z regułami w zapytaniach i ograniczeniach bazy?
- **Blokowanie.** Jak blokujesz i w którym miejscu kodu albo bazy?
- **Dane.** Jakie dane ma Twój kod i w jakiej postaci: tabela, obiekt, dokument, strumień zdarzeń?

Język i technologia są dowolne. Czego nie wiesz, dopytaj biznes.
