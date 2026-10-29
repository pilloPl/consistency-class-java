# Zadanie: naucz model szukać reguł spójnościowych

Po zadaniu 1 znasz kilka heurystyk, które odróżniają regułę spójnościową od zwykłego zapisu i wyświetlania danych:

1. **Test zmienności.** Czy dane, które sprawdzam, może w tej chwili zmienić inne żądanie? Tak: sprawdzenie i zapis muszą się odbyć w jednym kroku. Nie: zwykły zapis z walidacją.
2. **„Dwóch użytkowników” to tylko przykład.** Wystarczy jeden użytkownik z dwoma równoległymi żądaniami: podwójne kliknięcie, dwie karty przeglądarki.
3. **Zwykły zapis może mieć mnóstwo walidacji.** Sumy kontrolne, formaty, reguły krzyżowe na danych z żądania to nadal zwykły zapis.
4. **Ukryty zwykły zapis.** Jeden ekran miesza pola opisowe z polami sterowanymi regułami (status, akceptacje). To dwa modele.
5. **Test źródła prawdy.** Usuń widok i odbuduj go z danych źródłowych. Jeśli coś przepadło, to nie jest tylko widok.
6. **Dane do wyświetlenia poza kawałkiem z regułami.** Kawałek odpowiada tylko na pytanie „czy wolno”. Resztę dociąga aplikacja albo osobny widok.

**To nie jest pełna lista.** Uzupełnij ją o własne heurystyki: co jeszcze trzeba wziąć pod uwagę, żeby znaleźć regułę spójnościową albo odrzucić fałszywy trop? Przypomnij sobie, na czym się pomyliłeś w zadaniu 1 i co pomogło Ci to zauważyć. Dopisz co najmniej trzy.

**Skoro wiesz, czego szukać, możesz sprawdzić pracę narzędzia.**

---

# Polecenie

Napisz polecenie dla modelu językowego, które uczy go Twoich heurystyk: tych sześciu i tych, które dopisałeś. Model dostaje wymagania biznesowe programu „Razem” z zadania 1 i ma wskazać:

- operacje, które sprawdzają regułę spójnościową, i dane, które przy każdej z nich trzeba sprawdzić i zmienić w jednym kroku,
- operacje, które są zwykłym zapisem, odczytem albo wyliczeniem,
- miejsca, w których z samych wymagań nie da się tego rozstrzygnąć, razem z pytaniem do biznesu.

Polecenie ma nauczyć model heurystyk, a nie podać mu odpowiedzi. Nie wpisuj do niego niczego, co wiesz o „Razem” z omówienia.

**Sprawdź wynik.** Uruchom polecenie na wymaganiach i porównaj wynik ze swoim rozwiązaniem z zadania 1. Przy każdej pomyłce modelu zapisz, która heurystyka pozwoliła Ci ją wyłapać. Jeśli żadna, dopisz brakującą. Popraw polecenie i uruchom je jeszcze raz.

**Na koniec uruchom na tych samych wymaganiach skill `problem-classifier`** z repozytorium https://github.com/pilloPl/granice-spojnosci-skille (instrukcja instalacji jest w README).

Porównaj jego wynik z wynikiem swojego polecenia. O co skill zapytał, a Twoje polecenie nie? Co Twoje polecenie wyłapało, a skill przeoczył? Których heurystyk skilla brakowało na Twojej liście?
