# Příspěvky do Sedlogramu

Nejprve vám chceme poděkovat za váš zájem přispět k vývoji projektu **Sedlogram**!  
Následující instrukce vám pomohou s tím, jak postupovat při podávání návrhů, reportování chyb a rozšiřování kódu.

---

## Obsah

1. [Obecné zásady](#obecné-zásady)  
2. [Nahlášení problému (Issue)](#nahlášení-problému-issue)  
3. [Příprava vývojového prostředí](#příprava-vývojového-prostředí)  
4. [Pravidla pro psaní kódu](#pravidla-pro-psaní-kódu)  
5. [Testování](#testování)  
6. [Pull Requesty](#pull-requesty)  
7. [Diskuse a podpora](#diskuse-a-podpora)  
8. [Licence](#licence)  

---

## Obecné zásady

- **Respekt a slušnost**: Při komunikaci (Issues, Pull Requesty atp.) udržujte slušný a přátelský tón.
- **Transparentnost**: Při nahlášení chyby či navrhování nové funkce popište detailně motivaci, problém a možný způsob řešení.
- **Kvalitní dokumentace**: Každý nový příspěvek by měl obsahovat adekvátní komentáře a případnou úpravu dokumentace, aby byl kód srozumitelný a dobře udržovatelný.

---

## Nahlášení problému (Issue)

1. **Vyhledání duplicit**: Než nahlásíte chybu, projděte existující [Issues](../../issues) a ověřte, zda už není nahlášena.
2. **Popis problému**: Pokud je chyba nová, vytvořte **Issue** s co nejdetailnějším popisem:
   - Krátký, výstižný titulek
   - Informaci o verzi Javy a prostředí, ve kterém chyba nastala
   - Kroky k reprodukci
   - Očekávané chování vs. skutečné chování
   - Pokud je to možné, přiložte screenshoty nebo logy (stacktrace)
3. **Návrhy řešení**: Pokud máte představu, jak chybu opravit nebo funkci vylepšit, přidejte návrh řešení.

---

## Příprava vývojového prostředí

K vývoji Sedlogramu budete potřebovat:

- **Java 17 nebo vyšší** – ověřte si, že máte nainstalovanou požadovanou verzi JDK.  
- **Maven** – projekt používá Maven jako buildovací nástroj.  
- **IDE** (např. IntelliJ IDEA, Eclipse, VS Code atd.).

### Doporučený postup:

1. **Fork**: Vytvořte si fork repozitáře Sedlogram na GitHubu.
2. **Klonování**: Naklonujte váš fork do lokálního úložiště:
   ```bash
   git clone https://github.com/<vase-uzivatelske-jmeno>/sedlogram.git
