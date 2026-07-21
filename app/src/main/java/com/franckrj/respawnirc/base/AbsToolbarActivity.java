package com.franckrj.respawnirc.base;

import androidx.annotation.IdRes;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.franckrj.respawnirc.utils.Utils;

public abstract class AbsToolbarActivity extends AbsThemedActivity {
    protected Toolbar initToolbar(@IdRes int idOfToolbar) {
        Toolbar myToolbar = findViewById(idOfToolbar);
        setSupportActionBar(myToolbar);

        ActionBar myActionBar = getSupportActionBar();
        if (myActionBar != null) {
            myActionBar.setHomeButtonEnabled(true);
            myActionBar.setDisplayHomeAsUpEnabled(true);
        }

        /* Edge-to-edge : la barre d'état devient transparente, on ajoute donc l'inset du haut en
           padding sur la toolbar pour que son fond (colorPrimary) colore la bande de status bar et
           que son contenu reste dessous. Sans effet tant que l'edge-to-edge n'est pas actif (inset à 0).
           Le latéral est symétrique (max des insets gauche/droite de barre de navigation et découpe du
           capteur) pour que le contenu reste centré quel que soit le côté du capteur en paysage. */
        ViewCompat.setOnApplyWindowInsetsListener(myToolbar, (view, windowInsets) -> {
            int topInset = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout()).top;
            int side = Utils.getSymmetricSideInset(windowInsets);
            view.setPadding(side, topInset, side, 0);
            return windowInsets;
        });

        return myToolbar;
    }
}
