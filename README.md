# Smart Pantry

A JavaFX desktop app that tracks what's in your pantry and tells you which recipes you can actually cook right now — including the ones where you're only missing one or two ingredients.

## The interesting part: recipe matching

The core of this project isn't CRUD — it's `RecipeMatchingAlgorithm`, which scores every recipe against what's actually in the pantry:

- **Ingredient normalization** — lowercases, trims, and folds simple plurals (`tomatoes` → `tomato`) before comparing anything
- **Synonym awareness** — a small synonym map (`chicken` ≈ `chicken breast`/`chicken thigh`/`poultry`, `oil` ≈ `olive oil`/`vegetable oil`, etc.) so recipes don't fail to match over wording alone
- **Fuzzy matching** — falls back to Levenshtein distance for near-misses (typos, minor phrasing differences) when there's no exact or synonym match
- **Match percentage, not a boolean** — every recipe gets a `0–100%` score and a breakdown of exactly which ingredients matched and which are missing, sorted so the most-cookable recipes surface first

## Other features

- **Pantry inventory** — quantity, unit, and expiration date per product, with automatic "expiring soon" and "expired" flags
- **Recipe browser** filtered live by pantry contents
- Data persisted locally in **SQLite** — no account, no internet connection required

## Tech stack

- **JavaFX 21** (Controls + FXML) for the UI, styled with a custom stylesheet
- **Maven** build, packaged via the `javafx-maven-plugin`
- **SQLite** (via the `sqlite-jdbc` driver) for local storage — DAO layer (`ProductDAO`, `RecipeDAO`) sits on top of a single `DatabaseManager`

## Getting started

Requires **JDK 17+** and **Maven**.

```bash
git clone <this-repo>
cd "Smart Pantry"
mvn clean javafx:run
```

## Project structure

```
src/main/java/com/smartpantry/
  models/       Product, Recipe
  dao/          DatabaseManager, ProductDAO, RecipeDAO
  controllers/  MainController, PantryController, RecipeController
  utils/        RecipeMatchingAlgorithm, ValidationUtils, AlertUtils
src/main/resources/
  views/        FXML layouts
  styles/       application.css
```