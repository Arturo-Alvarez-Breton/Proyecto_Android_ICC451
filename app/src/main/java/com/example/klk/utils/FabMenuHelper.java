package com.example.klk.utils;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import androidx.cardview.widget.CardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * Helper class mejorado para manejar el menú desplegable del FloatingActionButton
 * Implementa animaciones fluidas y diseño atractivo con CardViews
 */
public class FabMenuHelper {

    private final FloatingActionButton fabMain;
    private final FloatingActionButton fabIndividualChat;
    private final FloatingActionButton fabCreateGroup;
    private final CardView cardIndividualChat;
    private final CardView cardCreateGroup;
    private final View overlay;

    private boolean isMenuOpen = false;
    private static final int ANIMATION_DURATION = 350;
    private static final int STAGGER_DELAY = 80;

    public FabMenuHelper(FloatingActionButton fabMain,
                        FloatingActionButton fabIndividualChat,
                        FloatingActionButton fabCreateGroup,
                        View cardIndividualChat,
                        View cardCreateGroup,
                        View overlay) {
        this.fabMain = fabMain;
        this.fabIndividualChat = fabIndividualChat;
        this.fabCreateGroup = fabCreateGroup;
        this.cardIndividualChat = (CardView) cardIndividualChat;
        this.cardCreateGroup = (CardView) cardCreateGroup;
        this.overlay = overlay;

        setupInitialState();
    }

    /**
     * Configura el estado inicial del menú
     */
    private void setupInitialState() {
        // Ocultar elementos del menú inicialmente
        cardIndividualChat.setVisibility(View.GONE);
        cardCreateGroup.setVisibility(View.GONE);
        overlay.setVisibility(View.GONE);

        // Configurar escalas iniciales para animaciones más suaves
        cardIndividualChat.setScaleX(0f);
        cardIndividualChat.setScaleY(0f);
        cardIndividualChat.setAlpha(0f);
        cardCreateGroup.setScaleX(0f);
        cardCreateGroup.setScaleY(0f);
        cardCreateGroup.setAlpha(0f);

        // Configurar transformaciones iniciales para efectos más dramáticos
        cardIndividualChat.setTranslationX(100f);
        cardIndividualChat.setTranslationY(50f);
        cardCreateGroup.setTranslationX(100f);
        cardCreateGroup.setTranslationY(50f);
    }

    /**
     * Alterna el estado del menú (abierto/cerrado)
     */
    public void toggleMenu() {
        if (isMenuOpen) {
            closeMenu();
        } else {
            openMenu();
        }
    }

    /**
     * Abre el menú con animaciones fluidas y atractivas
     */
    public void openMenu() {
        if (isMenuOpen) return;

        isMenuOpen = true;

        // Mostrar overlay con fade in suave
        overlay.setVisibility(View.VISIBLE);
        overlay.setAlpha(0f);
        ObjectAnimator.ofFloat(overlay, "alpha", 0f, 1f)
            .setDuration(ANIMATION_DURATION)
            .start();

        // Rotar el botón principal con animación suave
        ObjectAnimator rotation = ObjectAnimator.ofFloat(fabMain, "rotation", 0f, 135f);
        rotation.setDuration(ANIMATION_DURATION);
        rotation.setInterpolator(new OvershootInterpolator(1.2f));
        rotation.start();

        // Escalar el botón principal ligeramente
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(fabMain, "scaleX", 1f, 1.1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(fabMain, "scaleY", 1f, 1.1f);
        AnimatorSet scaleSet = new AnimatorSet();
        scaleSet.playTogether(scaleX, scaleY);
        scaleSet.setDuration(ANIMATION_DURATION);
        scaleSet.setInterpolator(new OvershootInterpolator());
        scaleSet.start();

        // Mostrar y animar cards del menú con efectos escalonados
        showMenuCard(cardCreateGroup, 0);
        showMenuCard(cardIndividualChat, STAGGER_DELAY);
    }

    /**
     * Cierra el menú con animaciones fluidas
     */
    public void closeMenu() {
        if (!isMenuOpen) return;

        isMenuOpen = false;

        // Ocultar overlay con fade out
        ObjectAnimator overlayAnimator = ObjectAnimator.ofFloat(overlay, "alpha", 1f, 0f);
        overlayAnimator.setDuration(ANIMATION_DURATION);
        overlayAnimator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                overlay.setVisibility(View.GONE);
            }
        });
        overlayAnimator.start();

        // Rotar el botón principal de vuelta
        ObjectAnimator rotation = ObjectAnimator.ofFloat(fabMain, "rotation", 135f, 0f);
        rotation.setDuration(ANIMATION_DURATION);
        rotation.setInterpolator(new OvershootInterpolator());
        rotation.start();

        // Restaurar escala del botón principal
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(fabMain, "scaleX", 1.1f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(fabMain, "scaleY", 1.1f, 1f);
        AnimatorSet scaleSet = new AnimatorSet();
        scaleSet.playTogether(scaleX, scaleY);
        scaleSet.setDuration(ANIMATION_DURATION);
        scaleSet.start();

        // Ocultar cards del menú con animaciones invertidas
        hideMenuCard(cardIndividualChat, 0);
        hideMenuCard(cardCreateGroup, STAGGER_DELAY);
    }

    /**
     * Muestra un card del menú con animación atractiva
     */
    private void showMenuCard(View card, int delay) {
        card.setVisibility(View.VISIBLE);

        // Crear conjunto de animaciones simultáneas
        AnimatorSet animatorSet = new AnimatorSet();

        // Animaciones de escala
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(card, "scaleX", 0f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(card, "scaleY", 0f, 1f);

        // Animaciones de translación (efecto de entrada desde la derecha)
        ObjectAnimator translateX = ObjectAnimator.ofFloat(card, "translationX", 100f, 0f);
        ObjectAnimator translateY = ObjectAnimator.ofFloat(card, "translationY", 50f, 0f);

        // Animación de alfa
        ObjectAnimator alpha = ObjectAnimator.ofFloat(card, "alpha", 0f, 1f);

        // Combinar todas las animaciones
        animatorSet.playTogether(scaleX, scaleY, translateX, translateY, alpha);
        animatorSet.setDuration(ANIMATION_DURATION);
        animatorSet.setStartDelay(delay);
        animatorSet.setInterpolator(new OvershootInterpolator(1.5f));
        animatorSet.start();
    }

    /**
     * Oculta un card del menú con animación atractiva
     */
    private void hideMenuCard(View card, int delay) {
        // Crear conjunto de animaciones simultáneas
        AnimatorSet animatorSet = new AnimatorSet();

        // Animaciones de escala
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(card, "scaleX", 1f, 0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(card, "scaleY", 1f, 0f);

        // Animaciones de translación (efecto de salida hacia la derecha)
        ObjectAnimator translateX = ObjectAnimator.ofFloat(card, "translationX", 0f, 100f);
        ObjectAnimator translateY = ObjectAnimator.ofFloat(card, "translationY", 0f, 50f);

        // Animación de alfa
        ObjectAnimator alpha = ObjectAnimator.ofFloat(card, "alpha", 1f, 0f);

        // Combinar todas las animaciones
        animatorSet.playTogether(scaleX, scaleY, translateX, translateY, alpha);
        animatorSet.setDuration(ANIMATION_DURATION);
        animatorSet.setStartDelay(delay);
        animatorSet.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                card.setVisibility(View.GONE);
            }
        });
        animatorSet.start();
    }

    /**
     * Verifica si el menú está abierto
     */
    public boolean isMenuOpen() {
        return isMenuOpen;
    }

    /**
     * Fuerza el cierre del menú sin animaciones
     */
    public void forceCloseMenu() {
        if (isMenuOpen) {
            isMenuOpen = false;
            overlay.setVisibility(View.GONE);
            fabMain.setRotation(0f);
            fabMain.setScaleX(1f);
            fabMain.setScaleY(1f);
            cardIndividualChat.setVisibility(View.GONE);
            cardCreateGroup.setVisibility(View.GONE);
        }
    }
}
