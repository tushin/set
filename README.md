# SET Game - Kotlin Multiplatform PWA

A modern, responsive implementation of the classic card game **SET** built with **Kotlin Multiplatform** and **Compose Multiplatform**.

This project demonstrates code sharing across Android and Web (Wasm) platforms, featuring a polished UI and smooth animations.

## About the Game

**SET** is a real-time card game designed by Marsha Falco in 1974. The goal is to identify a 'Set' of three cards from 12 laid out on the table. Each card has a variation of the following four features:

1.  **Color:** Red, Green, or Purple
2.  **Shape:** Diamond, Oval, or Squiggle
3.  **Number:** One, Two, or Three
4.  **Shading:** Solid, Striped, or Open

**A 'Set' consists of three cards in which each of the cards' features, look at one-by-one, are the same on each card, or are different on each card.**

For example:
*   **Color:** All Red OR one Red, one Green, one Purple.
*   **Shape:** All Ovals OR one Diamond, one Oval, one Squiggle.
*   **Number:** All Twos OR one One, one Two, one Three.
*   **Shading:** All Solid OR one Solid, one Striped, one Open.

If two cards are the same and one is different, it is **not** a Set.

## Project Structure

*   `composeApp`: Shared KMP module containing the game logic (`commonMain`), Android support (`androidMain`), and Web support (`wasmJsMain`).
*   `app`: Android application entry point.
*   `gradle`: Build configuration.

## Features

*   **Platform Independence:** Core logic and UI are shared 100% between Android and Web.
*   **Progressive Web App (PWA):** Installable on devices, offline support via Service Workers.
*   **Responsive Design:** Adapts layout for portrait (mobile) and landscape (desktop/tablet) screens.
*   **Animations:** Smooth transitions for card movements and hints.

## Technologies

*   Kotlin 2.0.21
*   Compose Multiplatform 1.7.0
*   Kotlin Wasm (WebAssembly)
*   Gradle 8.13

## How to Run

### Web (Wasm)
```bash
./gradlew :composeApp:wasmJsBrowserDevelopmentRun
```

### Android
Open in Android Studio and run the `app` configuration.

## Deployment

The project is configured to deploy automatically to GitHub Pages via GitHub Actions.
