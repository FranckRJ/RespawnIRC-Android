package com.franckrj.respawnirc;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.franckrj.respawnirc.utils.PrefsManager;

public class DraftUtils {
    private final PrefsManager.SaveDraftType saveDraftInfo;
    private final PrefsManager.BoolPref.Names useSavedDraftPref;
    private boolean dontOverrideUserChoiceOfUseSavedDraft = false;

    public DraftUtils(int defaultSaveDraftType, PrefsManager.BoolPref.Names newUseSavedDraftPref) {
        saveDraftInfo = new PrefsManager.SaveDraftType(defaultSaveDraftType);
        useSavedDraftPref = newUseSavedDraftPref;
    }

    public void loadPrefsInfos() {
        saveDraftInfo.setTypeFromString(PrefsManager.getString(PrefsManager.StringPref.Names.SAVE_MESSAGES_AND_TOPICS_AS_DRAFT_TYPE));
    }

    public boolean lastDraftSavedHasToBeUsed() {
        switch (saveDraftInfo.type) {
            case PrefsManager.SaveDraftType.ALWAYS:
                return true;
            case PrefsManager.SaveDraftType.NEVER:
                return false;
            default:
                return PrefsManager.getBool(useSavedDraftPref);
        }
    }

    public void afterDraftIsSaved() {
        if (!dontOverrideUserChoiceOfUseSavedDraft) {
            if (saveDraftInfo.type == PrefsManager.SaveDraftType.ALWAYS) {
                PrefsManager.putBool(useSavedDraftPref, true);
            } else {
                PrefsManager.putBool(useSavedDraftPref, false);
            }
        }
        dontOverrideUserChoiceOfUseSavedDraft = false;
    }

    public void whenUserTryToLeaveWithDraft(@StringRes final int idOfMessageShowedAfterDraftSaved, @StringRes final int idOfSaveDraftExplained, final Activity parentActivity) {
        switch (saveDraftInfo.type) {
            case PrefsManager.SaveDraftType.ALWAYS:
                Toast.makeText(parentActivity, parentActivity.getString(idOfMessageShowedAfterDraftSaved), Toast.LENGTH_SHORT).show();
                parentActivity.finish();
                break;
            case PrefsManager.SaveDraftType.ASK_BEFORE:
                final DialogInterface.OnClickListener onClickInSaveDraftConfirmationListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == DialogInterface.BUTTON_POSITIVE) {
                            PrefsManager.putBool(useSavedDraftPref, true);
                            dontOverrideUserChoiceOfUseSavedDraft = true;
                            Toast.makeText(parentActivity, parentActivity.getString(idOfMessageShowedAfterDraftSaved), Toast.LENGTH_SHORT).show();
                            parentActivity.finish();
                        } else if (which == DialogInterface.BUTTON_NEGATIVE) {
                            PrefsManager.putBool(useSavedDraftPref, false);
                            dontOverrideUserChoiceOfUseSavedDraft = true;
                            parentActivity.finish();
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(parentActivity);
                builder.setTitle(R.string.saveDraft).setMessage(idOfSaveDraftExplained)
                        .setPositiveButton(R.string.yes, onClickInSaveDraftConfirmationListener).setNegativeButton(R.string.no, onClickInSaveDraftConfirmationListener);
                builder.show();
                break;
            default:
                parentActivity.finish();
                break;
        }
    }
}
