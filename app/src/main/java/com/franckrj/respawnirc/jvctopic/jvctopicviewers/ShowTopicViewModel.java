package com.franckrj.respawnirc.jvctopic.jvctopicviewers;

import androidx.lifecycle.ViewModel;

import com.franckrj.respawnirc.utils.JVCParser;

import java.util.ArrayList;

public class ShowTopicViewModel extends ViewModel {
    public ArrayList<JVCParser.MessageInfos> listOfMessagesShowed = null;
}
