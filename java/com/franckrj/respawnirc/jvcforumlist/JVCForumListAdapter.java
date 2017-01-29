package com.franckrj.respawnirc.jvcforumlist;

import android.app.Activity;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.franckrj.respawnirc.R;
import com.franckrj.respawnirc.utils.JVCParser;
import com.franckrj.respawnirc.utils.Undeprecator;

import java.util.ArrayList;

public class JVCForumListAdapter extends BaseExpandableListAdapter implements ExpandableListView.OnGroupClickListener, ExpandableListView.OnChildClickListener {
    private static final String SAVE_LIST_OF_SEARCHED_FORUM_SHOWED = "saveListOfSearchedForumShowed";

    private ArrayList<ForumInfo> currentListOfForums = null;
    private ArrayList<ForumInfo> baseListOfForums = null;
    private ArrayList<JVCParser.NameAndLink> lastListOfForumsShowed = null;
    private LayoutInflater serviceInflater;
    private Activity parentActivity = null;

    public JVCForumListAdapter(Activity newParentActivity) {
        baseListOfForums = new ArrayList<>();
        parentActivity = newParentActivity;
        serviceInflater = (LayoutInflater) parentActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        {
            ForumInfo tmpForumInfo = new ForumInfo();
            tmpForumInfo.isTitle = true;
            tmpForumInfo.isForum = false;
            tmpForumInfo.forum = new JVCParser.NameAndLink("FORUMS BLABLA", "");
            baseListOfForums.add(tmpForumInfo);
        }
        {
            ForumInfo tmpForumInfo = new ForumInfo();
            tmpForumInfo.isTitle = false;
            tmpForumInfo.isForum = false;
            tmpForumInfo.forum = new JVCParser.NameAndLink("Par age", "");
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("Moins de 15 ans", "http://www.jeuxvideo.com/forums/0-15-0-1-0-1-0-blabla-moins-de-15-ans.htm"));
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("15 - 18 ans", "http://www.jeuxvideo.com/forums/0-50-0-1-0-1-0-blabla-15-18-ans.htm"));
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("18 - 25 ans", "http://www.jeuxvideo.com/forums/0-51-0-1-0-1-0-blabla-18-25-ans.htm"));
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("25 - 35 ans", "http://www.jeuxvideo.com/forums/0-52-0-1-0-1-0-blabla-25-35-ans.htm"));
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("Plus de 35 ans", "http://www.jeuxvideo.com/forums/0-53-0-1-0-1-0-blabla-35-ans-et-plus.htm"));
            baseListOfForums.add(tmpForumInfo);
        }
        {
            ForumInfo tmpForumInfo = new ForumInfo();
            tmpForumInfo.isTitle = false;
            tmpForumInfo.isForum = false;
            tmpForumInfo.forum = new JVCParser.NameAndLink("Par pays", "");
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("Belgique", "http://www.jeuxvideo.com/forums/0-1000020-0-1-0-1-0-belgique.htm"));
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("Suisse", "http://www.jeuxvideo.com/forums/0-1000022-0-1-0-1-0-suisse.htm"));
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("Québec", "http://www.jeuxvideo.com/forums/0-83-0-1-0-1-0-quebec.htm"));
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("Japon", "http://www.jeuxvideo.com/forums/0-1000034-0-1-0-1-0-japon.htm"));
            baseListOfForums.add(tmpForumInfo);
        }
        {
            ForumInfo tmpForumInfo = new ForumInfo();
            tmpForumInfo.isTitle = true;
            tmpForumInfo.isForum = false;
            tmpForumInfo.forum = new JVCParser.NameAndLink("JEUXVIDEO.COM", "");
            baseListOfForums.add(tmpForumInfo);
        }
        {
            ForumInfo tmpForumInfo = new ForumInfo();
            tmpForumInfo.isTitle = false;
            tmpForumInfo.isForum = true;
            tmpForumInfo.forum = new JVCParser.NameAndLink("Communauté", "http://www.jeuxvideo.com/forums/0-1000021-0-1-0-1-0-communaute.htm");
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("La Rédac et vous", "http://www.jeuxvideo.com/forums/0-99-0-1-0-1-0-la-redac-et-vous.htm"));
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("Boutique jeuxvideo.com", "http://www.jeuxvideo.com/forums/0-1000047-0-1-0-1-0-boutique-jeuxvideo-com.htm"));
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("Suggestions jeuxvideo.com", "http://www.jeuxvideo.com/forums/0-13-0-1-0-1-0-suggestions-jeuxvideo-com.htm"));
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("Sondages", "http://www.jeuxvideo.com/forums/0-76-0-1-0-1-0-sondages.htm"));
            baseListOfForums.add(tmpForumInfo);
        }
        {
            ForumInfo tmpForumInfo = new ForumInfo();
            tmpForumInfo.isTitle = false;
            tmpForumInfo.isForum = true;
            tmpForumInfo.forum = new JVCParser.NameAndLink("Contributions utilisateurs", "http://www.jeuxvideo.com/forums/0-1000002-0-1-0-1-0-contribuer-a-jeuxvideo-com.htm");
            baseListOfForums.add(tmpForumInfo);
        }
        {
            ForumInfo tmpForumInfo = new ForumInfo();
            tmpForumInfo.isTitle = false;
            tmpForumInfo.isForum = true;
            tmpForumInfo.forum = new JVCParser.NameAndLink("Aide aux utilisateurs", "http://www.jeuxvideo.com/forums/0-1000017-0-1-0-1-0-aide-aux-utilisateurs.htm");
            baseListOfForums.add(tmpForumInfo);
        }
        {
            ForumInfo tmpForumInfo = new ForumInfo();
            tmpForumInfo.isTitle = true;
            tmpForumInfo.isForum = false;
            tmpForumInfo.forum = new JVCParser.NameAndLink("LE JEU VIDÉO", "");
            baseListOfForums.add(tmpForumInfo);
        }
        {
            ForumInfo tmpForumInfo = new ForumInfo();
            tmpForumInfo.isTitle = false;
            tmpForumInfo.isForum = true;
            tmpForumInfo.forum = new JVCParser.NameAndLink("Général jeu vidéo", "http://www.jeuxvideo.com/forums/0-7-0-1-0-1-0-general-jeux-video.htm");
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("FPS & TPS", "http://www.jeuxvideo.com/forums/0-92-0-1-0-1-0-fps-tps.htm"));
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("RPG", "http://www.jeuxvideo.com/forums/0-93-0-1-0-1-0-rpg-role-playing-game.htm"));
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("STR", "http://www.jeuxvideo.com/forums/0-91-0-1-0-1-0-str-strategie-temps-reel.htm"));
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("Jeux de combat", "http://www.jeuxvideo.com/forums/0-1000028-0-1-0-1-0-jeux-de-combat.htm"));
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("Jeux de courses", "http://www.jeuxvideo.com/forums/0-95-0-1-0-1-0-jeux-de-courses.htm"));
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("Jeux d'aventure", "http://www.jeuxvideo.com/forums/0-94-0-1-0-1-0-jeux-d-aventure.htm"));
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("Jeux de sports", "http://www.jeuxvideo.com/forums/0-96-0-1-0-1-0-jeux-de-sports.htm"));
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("Simulation", "http://www.jeuxvideo.com/forums/0-1000013-0-1-0-1-0-simulation.htm"));
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("Musiques de jeux vidéo", "http://www.jeuxvideo.com/forums/0-1000007-0-1-0-1-0-musiques-de-jeux-video.htm"));
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("Magazine de jeux vidéo", "http://www.jeuxvideo.com/forums/0-77-0-1-0-1-0-magazines-de-jeux-video.htm"));
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("Import", "http://www.jeuxvideo.com/forums/0-75-0-1-0-1-0-import.htm"));
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("Héros de jeux vidéo", "http://www.jeuxvideo.com/forums/0-72-0-1-0-1-0-heros-de-jeux-video.htm"));
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("Business du jeu vidéo", "http://www.jeuxvideo.com/forums/0-49-0-1-0-1-0-business-du-jeu-video.htm"));
            baseListOfForums.add(tmpForumInfo);
        }
        {
            ForumInfo tmpForumInfo = new ForumInfo();
            tmpForumInfo.isTitle = false;
            tmpForumInfo.isForum = true;
            tmpForumInfo.forum = new JVCParser.NameAndLink("Oldies & rétrogaming", "http://www.jeuxvideo.com/forums/0-1000010-0-1-0-1-0-retrogaming.htm");
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("Nintendo DS", "http://www.jeuxvideo.com/forums/0-56-0-1-0-1-0-nintendo-ds.htm"));
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("PlayStation 2", "http://www.jeuxvideo.com/forums/0-18-0-1-0-1-0-playstation-2.htm"));
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("Xbox", "http://www.jeuxvideo.com/forums/0-10-0-1-0-1-0-xbox.htm"));
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("Gamecube", "http://www.jeuxvideo.com/forums/0-5-0-1-0-1-0-gamecube.htm"));
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("Gameboy Advance", "http://www.jeuxvideo.com/forums/0-14-0-1-0-1-0-gameboy-advance.htm"));
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("PlayStation 1", "http://www.jeuxvideo.com/forums/0-8-0-1-0-1-0-playstation.htm"));
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("Dreamcast", "http://www.jeuxvideo.com/forums/0-12-0-1-0-1-0-dreamcast.htm"));
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("Playstation Portable", "http://www.jeuxvideo.com/forums/0-42-0-1-0-1-0-playstation-portable.htm"));
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("Nintendo 64", "http://www.jeuxvideo.com/forums/0-9-0-1-0-1-0-nintendo-64.htm"));
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("Megadrive", "http://www.jeuxvideo.com/forums/0-1000025-0-1-0-1-0-megadrive.htm"));
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("Super Nintendo", "http://www.jeuxvideo.com/forums/0-1000026-0-1-0-1-0-super-nintendo.htm"));
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("Saturn", "http://www.jeuxvideo.com/forums/0-1000027-0-1-0-1-0-saturn.htm"));
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("Gameboy", "http://www.jeuxvideo.com/forums/0-19-0-1-0-1-0-gameboy.htm"));
            baseListOfForums.add(tmpForumInfo);
        }
        {
            ForumInfo tmpForumInfo = new ForumInfo();
            tmpForumInfo.isTitle = false;
            tmpForumInfo.isForum = true;
            tmpForumInfo.forum = new JVCParser.NameAndLink("Salons & événements", "http://www.jeuxvideo.com/forums/0-1000049-0-1-0-1-0-salons-et-evenements.htm");
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("E3", "http://www.jeuxvideo.com/forums/0-1000051-0-1-0-1-0-e3.htm"));
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("Gamescom", "http://www.jeuxvideo.com/forums/0-1000053-0-1-0-1-0-gamescom.htm"));
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("Tokyo Game Show", "http://www.jeuxvideo.com/forums/0-1000052-0-1-0-1-0-tokyo-game-show.htm"));
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("Paris Games Week", "http://www.jeuxvideo.com/forums/0-1000050-0-1-0-1-0-paris-games-week.htm"));
            baseListOfForums.add(tmpForumInfo);
        }
        {
            ForumInfo tmpForumInfo = new ForumInfo();
            tmpForumInfo.isTitle = false;
            tmpForumInfo.isForum = true;
            tmpForumInfo.forum = new JVCParser.NameAndLink("Jeux en ligne", "http://www.jeuxvideo.com/forums/0-4-0-1-0-1-0-jeu-en-ligne.htm");
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("E-sport", "http://www.jeuxvideo.com/forums/0-84-0-1-0-1-0-e-sport.htm"));
            baseListOfForums.add(tmpForumInfo);
        }
        {
            ForumInfo tmpForumInfo = new ForumInfo();
            tmpForumInfo.isTitle = false;
            tmpForumInfo.isForum = true;
            tmpForumInfo.forum = new JVCParser.NameAndLink("Petites annonces", "http://www.jeuxvideo.com/forums/0-3-0-1-0-1-0-petites-annonces.htm");
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("Collectionneurs", "http://www.jeuxvideo.com/forums/0-64-0-1-0-1-0-collectionneurs.htm"));
            baseListOfForums.add(tmpForumInfo);
        }
        {
            ForumInfo tmpForumInfo = new ForumInfo();
            tmpForumInfo.isTitle = false;
            tmpForumInfo.isForum = true;
            tmpForumInfo.forum = new JVCParser.NameAndLink("Guerre des consoles", "http://www.jeuxvideo.com/forums/0-36-0-1-0-1-0-guerre-des-consoles.htm");
            baseListOfForums.add(tmpForumInfo);
        }
        {
            ForumInfo tmpForumInfo = new ForumInfo();
            tmpForumInfo.isTitle = false;
            tmpForumInfo.isForum = true;
            tmpForumInfo.forum = new JVCParser.NameAndLink("Jeux indépendants", "http://www.jeuxvideo.com/forums/0-3000472-0-1-0-1-0-jeux-independants.htm");
            baseListOfForums.add(tmpForumInfo);
        }
        {
            ForumInfo tmpForumInfo = new ForumInfo();
            tmpForumInfo.isTitle = false;
            tmpForumInfo.isForum = true;
            tmpForumInfo.forum = new JVCParser.NameAndLink("Free to play", "http://www.jeuxvideo.com/forums/0-97-0-1-0-1-0-free-to-play.htm");
            baseListOfForums.add(tmpForumInfo);
        }
        {
            ForumInfo tmpForumInfo = new ForumInfo();
            tmpForumInfo.isTitle = false;
            tmpForumInfo.isForum = true;
            tmpForumInfo.forum = new JVCParser.NameAndLink("Jeu vidéo & handicap", "http://www.jeuxvideo.com/forums/0-74-0-1-0-1-0-jeux-video-et-handicap.htm");
            baseListOfForums.add(tmpForumInfo);
        }
        {
            ForumInfo tmpForumInfo = new ForumInfo();
            tmpForumInfo.isTitle = true;
            tmpForumInfo.isForum = false;
            tmpForumInfo.forum = new JVCParser.NameAndLink("MACHINES", "");
            baseListOfForums.add(tmpForumInfo);
        }
        {
            ForumInfo tmpForumInfo = new ForumInfo();
            tmpForumInfo.isTitle = false;
            tmpForumInfo.isForum = true;
            tmpForumInfo.forum = new JVCParser.NameAndLink("Jeux PC", "http://www.jeuxvideo.com/forums/0-3000397-0-1-0-1-0-jeux-pc.htm");
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("Plateforme de Téléchargement PC", "http://www.jeuxvideo.com/forums/0-1000040-0-1-0-1-0-plateforme-de-telechargement-pc.htm"));
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("Oculus Rift", "http://www.jeuxvideo.com/forums/0-1000046-0-1-0-1-0-oculus-rift.htm"));
            baseListOfForums.add(tmpForumInfo);
        }
        {
            ForumInfo tmpForumInfo = new ForumInfo();
            tmpForumInfo.isTitle = false;
            tmpForumInfo.isForum = true;
            tmpForumInfo.forum = new JVCParser.NameAndLink("Xbox One", "http://www.jeuxvideo.com/forums/0-1000044-0-1-0-1-0-xbox-one.htm");
            baseListOfForums.add(tmpForumInfo);
        }
        {
            ForumInfo tmpForumInfo = new ForumInfo();
            tmpForumInfo.isTitle = false;
            tmpForumInfo.isForum = true;
            tmpForumInfo.forum = new JVCParser.NameAndLink("Xbox 360", "http://www.jeuxvideo.com/forums/0-61-0-1-0-1-0-xbox-360.htm");
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("Xbox Live Arcade", "http://www.jeuxvideo.com/forums/0-1000000-0-1-0-1-0-xbox-live-arcade.htm"));
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("Xbox Originals", "http://www.jeuxvideo.com/forums/0-1000023-0-1-0-1-0-xbox-originals.htm"));
            baseListOfForums.add(tmpForumInfo);
        }
        {
            ForumInfo tmpForumInfo = new ForumInfo();
            tmpForumInfo.isTitle = false;
            tmpForumInfo.isForum = true;
            tmpForumInfo.forum = new JVCParser.NameAndLink("PlayStation 4", "http://www.jeuxvideo.com/forums/0-1000043-0-1-0-1-0-playstation-4.htm");
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("Playstation VR", "http://www.jeuxvideo.com/forums/0-3000032-0-1-0-1-0-playstation-vr.htm"));
            baseListOfForums.add(tmpForumInfo);
        }
        {
            ForumInfo tmpForumInfo = new ForumInfo();
            tmpForumInfo.isTitle = false;
            tmpForumInfo.isForum = true;
            tmpForumInfo.forum = new JVCParser.NameAndLink("PlayStation 3", "http://www.jeuxvideo.com/forums/0-60-0-1-0-1-0-playstation-3.htm");
            baseListOfForums.add(tmpForumInfo);
        }
        {
            ForumInfo tmpForumInfo = new ForumInfo();
            tmpForumInfo.isTitle = false;
            tmpForumInfo.isForum = true;
            tmpForumInfo.forum = new JVCParser.NameAndLink("PlayStation Vita", "http://www.jeuxvideo.com/forums/0-1000006-0-1-0-1-0-playstation-vita.htm");
            baseListOfForums.add(tmpForumInfo);
        }
        {
            ForumInfo tmpForumInfo = new ForumInfo();
            tmpForumInfo.isTitle = false;
            tmpForumInfo.isForum = true;
            tmpForumInfo.forum = new JVCParser.NameAndLink("PlayStation Store", "http://www.jeuxvideo.com/forums/0-1000015-0-1-0-1-0-playstation-store.htm");
            baseListOfForums.add(tmpForumInfo);
        }
        {
            ForumInfo tmpForumInfo = new ForumInfo();
            tmpForumInfo.isTitle = false;
            tmpForumInfo.isForum = true;
            tmpForumInfo.forum = new JVCParser.NameAndLink("Wii U", "http://www.jeuxvideo.com/forums/0-1000011-0-1-0-1-0-wii-u.htm");
            baseListOfForums.add(tmpForumInfo);
        }
        {
            ForumInfo tmpForumInfo = new ForumInfo();
            tmpForumInfo.isTitle = false;
            tmpForumInfo.isForum = true;
            tmpForumInfo.forum = new JVCParser.NameAndLink("Wii", "http://www.jeuxvideo.com/forums/0-62-0-1-0-1-0-wii.htm");
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("Wii Ware & Console virtuelle", "http://www.jeuxvideo.com/forums/0-1000024-0-1-0-1-0-wii-ware-console-virtuelle.htm"));
            baseListOfForums.add(tmpForumInfo);
        }
        {
            ForumInfo tmpForumInfo = new ForumInfo();
            tmpForumInfo.isTitle = false;
            tmpForumInfo.isForum = true;
            tmpForumInfo.forum = new JVCParser.NameAndLink("3DS", "http://www.jeuxvideo.com/forums/0-1000039-0-1-0-1-0-nintendo-3ds.htm");
            baseListOfForums.add(tmpForumInfo);
        }
        {
            ForumInfo tmpForumInfo = new ForumInfo();
            tmpForumInfo.isTitle = false;
            tmpForumInfo.isForum = true;
            tmpForumInfo.forum = new JVCParser.NameAndLink("Nintendo Switch", "http://www.jeuxvideo.com/forums/0-3007199-0-1-0-1-0-nintendo-switch.htm");
            baseListOfForums.add(tmpForumInfo);
        }
        {
            ForumInfo tmpForumInfo = new ForumInfo();
            tmpForumInfo.isTitle = false;
            tmpForumInfo.isForum = true;
            tmpForumInfo.forum = new JVCParser.NameAndLink("Smartphones & tablettes", "http://www.jeuxvideo.com/forums/0-17-0-1-0-1-0-smartphone-tablettes.htm");
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("iPhone / iPod / iPad / Watch", "http://www.jeuxvideo.com/forums/0-1000029-0-1-0-1-0-iphone-ipod-ipad-watch.htm"));
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("Android", "http://www.jeuxvideo.com/forums/0-1000005-0-1-0-1-0-android.htm"));
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("Windows Phone", "http://www.jeuxvideo.com/forums/0-1000041-0-1-0-1-0-windows-phone.htm"));
            baseListOfForums.add(tmpForumInfo);
        }
        {
            ForumInfo tmpForumInfo = new ForumInfo();
            tmpForumInfo.isTitle = true;
            tmpForumInfo.isForum = false;
            tmpForumInfo.forum = new JVCParser.NameAndLink("PASSIONS", "");
            baseListOfForums.add(tmpForumInfo);
        }
        {
            ForumInfo tmpForumInfo = new ForumInfo();
            tmpForumInfo.isTitle = false;
            tmpForumInfo.isForum = true;
            tmpForumInfo.forum = new JVCParser.NameAndLink("Informatique", "http://www.jeuxvideo.com/forums/0-1-0-1-0-1-0-informatique.htm");
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("Matériel informatique", "http://www.jeuxvideo.com/forums/0-6-0-1-0-1-0-materiel-informatique.htm"));
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("Mac", "http://www.jeuxvideo.com/forums/0-11-0-1-0-1-0-macintosh.htm"));
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("Linux", "http://www.jeuxvideo.com/forums/0-38-0-1-0-1-0-linux.htm"));
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("Steam OS", "http://www.jeuxvideo.com/forums/0-1000048-0-1-0-1-0-steam-os.htm"));
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("Création de jeux", "http://www.jeuxvideo.com/forums/0-31-0-1-0-1-0-creation-de-jeux.htm"));
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("Création de sites web", "http://www.jeuxvideo.com/forums/0-30-0-1-0-1-0-creation-de-sites-web.htm"));
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("Programmation", "http://www.jeuxvideo.com/forums/0-47-0-1-0-1-0-programmation.htm"));
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("Internet", "http://www.jeuxvideo.com/forums/0-90-0-1-0-1-0-internet.htm"));
            baseListOfForums.add(tmpForumInfo);
        }
        {
            ForumInfo tmpForumInfo = new ForumInfo();
            tmpForumInfo.isTitle = false;
            tmpForumInfo.isForum = true;
            tmpForumInfo.forum = new JVCParser.NameAndLink("Sport", "http://www.jeuxvideo.com/forums/0-24-0-1-0-1-0-sport.htm");
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("Football", "http://www.jeuxvideo.com/forums/0-20-0-1-0-1-0-football.htm"));
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("Catch", "http://www.jeuxvideo.com/forums/0-79-0-1-0-1-0-catch.htm"));
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("Musculation & nutrition", "http://www.jeuxvideo.com/forums/0-78-0-1-0-1-0-musculation-nutrition.htm"));
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("Tennis", "http://www.jeuxvideo.com/forums/0-33-0-1-0-1-0-tennis.htm"));
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("Basket", "http://www.jeuxvideo.com/forums/0-29-0-1-0-1-0-basket.htm"));
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("Rugby", "http://www.jeuxvideo.com/forums/0-21-0-1-0-1-0-rugby.htm"));
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("Boxe", "http://www.jeuxvideo.com/forums/0-88-0-1-0-1-0-boxe.htm"));
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("Cyclisme", "http://www.jeuxvideo.com/forums/0-46-0-1-0-1-0-cyclisme.htm"));
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("Sports de glisse", "http://www.jeuxvideo.com/forums/0-23-0-1-0-1-0-sports-de-glisse.htm"));
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("Sports mécaniques", "http://www.jeuxvideo.com/forums/0-22-0-1-0-1-0-sports-mecaniques.htm"));
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("Tennis de table", "http://www.jeuxvideo.com/forums/0-82-0-1-0-1-0-tennis-de-table.htm"));
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("Arts martiaux / Sports de combat", "http://www.jeuxvideo.com/forums/0-45-0-1-0-1-0-arts-martiaux-sports-de-combat.htm"));
            baseListOfForums.add(tmpForumInfo);
        }
        {
            ForumInfo tmpForumInfo = new ForumInfo();
            tmpForumInfo.isTitle = false;
            tmpForumInfo.isForum = true;
            tmpForumInfo.forum = new JVCParser.NameAndLink("Art", "http://www.jeuxvideo.com/forums/0-3000476-0-1-0-1-0-art.htm");
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("Cinéma", "http://www.jeuxvideo.com/forums/0-26-0-1-0-1-0-cinema.htm"));
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("Télévision & séries", "http://www.jeuxvideo.com/forums/0-32-0-1-0-1-0-television-series.htm"));
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("Animation", "http://www.jeuxvideo.com/forums/0-98-0-1-0-1-0-animation.htm"));
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("Musique", "http://www.jeuxvideo.com/forums/0-25-0-1-0-1-0-musique.htm"));
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("Livres", "http://www.jeuxvideo.com/forums/0-34-0-1-0-1-0-livres.htm"));
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("BD - Mangas - Comics", "http://www.jeuxvideo.com/forums/0-27-0-1-0-1-0-bd-mangas-comics.htm"));
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("Photographie", "http://www.jeuxvideo.com/forums/0-1000009-0-1-0-1-0-photographie.htm"));
            baseListOfForums.add(tmpForumInfo);
        }
        {
            ForumInfo tmpForumInfo = new ForumInfo();
            tmpForumInfo.isTitle = false;
            tmpForumInfo.isForum = true;
            tmpForumInfo.forum = new JVCParser.NameAndLink("Savoir & Culture", "http://www.jeuxvideo.com/forums/0-3000481-0-1-0-1-0-savoir-culture.htm");
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("Cours & devoirs", "http://www.jeuxvideo.com/forums/0-35-0-1-0-1-0-cours-et-devoirs.htm"));
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("Métiers & Orientation", "http://www.jeuxvideo.com/forums/0-70-0-1-0-1-0-metiers-orientation.htm"));
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("Histoire", "http://www.jeuxvideo.com/forums/0-59-0-1-0-1-0-histoire.htm"));
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("Philosophie", "http://www.jeuxvideo.com/forums/0-68-0-1-0-1-0-philosophie.htm"));
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("Politique", "http://www.jeuxvideo.com/forums/0-55-0-1-0-1-0-politique.htm"));
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("Environnement & Nature", "http://www.jeuxvideo.com/forums/0-1000001-0-1-0-1-0-environnement-nature.htm"));
            baseListOfForums.add(tmpForumInfo);
        }
        {
            ForumInfo tmpForumInfo = new ForumInfo();
            tmpForumInfo.isTitle = false;
            tmpForumInfo.isForum = true;
            tmpForumInfo.forum = new JVCParser.NameAndLink("Sciences & Techno", "http://www.jeuxvideo.com/forums/0-65-0-1-0-1-0-sciences-technologies.htm");
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("Astronomie", "http://www.jeuxvideo.com/forums/0-57-0-1-0-1-0-astronomie.htm"));
            baseListOfForums.add(tmpForumInfo);
        }
        {
            ForumInfo tmpForumInfo = new ForumInfo();
            tmpForumInfo.isTitle = false;
            tmpForumInfo.isForum = true;
            tmpForumInfo.forum = new JVCParser.NameAndLink("Loisirs", "http://www.jeuxvideo.com/forums/0-3000473-0-1-0-1-0-loisirs.htm");
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("TV - HiFi- Home Cinema", "http://www.jeuxvideo.com/forums/0-28-0-1-0-1-0-tv-hifi-home-cinema.htm"));
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("Automobiles", "http://www.jeuxvideo.com/forums/0-1000019-0-1-0-1-0-automobiles.htm"));
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("Cuisine & Pâtisserie", "http://www.jeuxvideo.com/forums/0-67-0-1-0-1-0-cuisine-et-patisserie.htm"));
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("Nature & Animaux", "http://www.jeuxvideo.com/forums/0-39-0-1-0-1-0-nature-et-animaux.htm"));
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("Humour", "http://www.jeuxvideo.com/forums/0-54-0-1-0-1-0-humour.htm"));
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("Voyages", "http://www.jeuxvideo.com/forums/0-80-0-1-0-1-0-voyage.htm"));
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("Aviation", "http://www.jeuxvideo.com/forums/0-1000004-0-1-0-1-0-aviation.htm"));
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("Paranormal", "http://www.jeuxvideo.com/forums/0-89-0-1-0-1-0-paranormal.htm"));
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("Jeux", "http://www.jeuxvideo.com/forums/0-71-0-1-0-1-0-jeux.htm"));
            baseListOfForums.add(tmpForumInfo);
        }
        {
            ForumInfo tmpForumInfo = new ForumInfo();
            tmpForumInfo.isTitle = false;
            tmpForumInfo.isForum = true;
            tmpForumInfo.forum = new JVCParser.NameAndLink("Création", "http://www.jeuxvideo.com/forums/0-3000405-0-1-0-1-0-creation.htm");
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("Arts Graphiques", "http://www.jeuxvideo.com/forums/0-48-0-1-0-1-0-arts-graphiques.htm"));
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("Écriture", "http://www.jeuxvideo.com/forums/0-58-0-1-0-1-0-ecriture.htm"));
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("Modélisme", "http://www.jeuxvideo.com/forums/0-1000031-0-1-0-1-0-modelisme.htm"));
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("Tuning PC", "http://www.jeuxvideo.com/forums/0-66-0-1-0-1-0-tuning-pc.htm"));
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("Montage vidéo", "http://www.jeuxvideo.com/forums/0-1000030-0-1-0-1-0-montage-video.htm"));
            baseListOfForums.add(tmpForumInfo);
        }
        {
            ForumInfo tmpForumInfo = new ForumInfo();
            tmpForumInfo.isTitle = false;
            tmpForumInfo.isForum = true;
            tmpForumInfo.forum = new JVCParser.NameAndLink("Actualités", "http://www.jeuxvideo.com/forums/0-69-0-1-0-1-0-actualites.htm");
            baseListOfForums.add(tmpForumInfo);
        }
        {
            ForumInfo tmpForumInfo = new ForumInfo();
            tmpForumInfo.isTitle = false;
            tmpForumInfo.isForum = true;
            tmpForumInfo.forum = new JVCParser.NameAndLink("Santé & Bien-être", "http://www.jeuxvideo.com/forums/0-3002340-0-1-0-1-0-sante-et-bien-etre.htm");
            tmpForumInfo.subForumList.add(new JVCParser.NameAndLink("Sexualité", "http://www.jeuxvideo.com/forums/0-3002014-0-1-0-1-0-sexualite.htm"));
            baseListOfForums.add(tmpForumInfo);
        }

        currentListOfForums = baseListOfForums;
    }

    public void setNewListOfForums(ArrayList<JVCParser.NameAndLink> newListOfForums) {
        lastListOfForumsShowed = newListOfForums;

        if (lastListOfForumsShowed == null) {
            currentListOfForums = baseListOfForums;
        } else {
            currentListOfForums = new ArrayList<>();

            if (!lastListOfForumsShowed.isEmpty()) {
                for (JVCParser.NameAndLink thisForum : lastListOfForumsShowed) {
                    ForumInfo tmpForumInfo = new ForumInfo();
                    tmpForumInfo.isTitle = false;
                    tmpForumInfo.isForum = true;
                    tmpForumInfo.forum = thisForum;
                    currentListOfForums.add(tmpForumInfo);
                }
            } else {
                ForumInfo tmpForumInfo = new ForumInfo();
                tmpForumInfo.isTitle = false;
                tmpForumInfo.isForum = false;
                tmpForumInfo.forum = new JVCParser.NameAndLink(parentActivity.getString(R.string.noResultFound), "");
                currentListOfForums.add(tmpForumInfo);
            }
        }

        notifyDataSetChanged();
    }

    public void clearListOfForums() {
        currentListOfForums = new ArrayList<>();
        notifyDataSetChanged();
    }

    public void saveToBundle(Bundle savedInstanceState) {
        savedInstanceState.putParcelableArrayList(SAVE_LIST_OF_SEARCHED_FORUM_SHOWED, lastListOfForumsShowed);
    }

    public void loadFromBundle(Bundle savedInstanceState) {
        lastListOfForumsShowed = savedInstanceState.getParcelableArrayList(SAVE_LIST_OF_SEARCHED_FORUM_SHOWED);
        setNewListOfForums(lastListOfForumsShowed);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return currentListOfForums.get(groupPosition).subForumList.get(childPosition).name;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ViewHolder holder;
        String childTitle = (String) getChild(groupPosition, childPosition);

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = serviceInflater.inflate(R.layout.jvcforums_row, parent, false);
            holder.itemOne = (TextView) convertView.findViewById(R.id.item_one_jvcforums_text_row);
            holder.imageButtonExpand = (ImageView) convertView.findViewById(R.id.image_button_expand_jvcforums_text_row);
            holder.isForumImage = (ImageView) convertView.findViewById(R.id.image_is_forum_jvcforums_text_row);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        convertView.setBackgroundColor(Undeprecator.resourcesGetColor(parentActivity.getResources(), R.color.darkerColorForBackground));
        holder.itemOne.setTypeface(null, Typeface.NORMAL);
        holder.itemOne.setPaintFlags(holder.itemOne.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));
        holder.itemOne.setText(childTitle);
        holder.imageButtonExpand.setVisibility(View.INVISIBLE);
        holder.isForumImage.setVisibility(View.GONE);
        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return currentListOfForums.get(groupPosition).subForumList.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return currentListOfForums.get(groupPosition).forum.name;
    }

    @Override
    public int getGroupCount() {
        return currentListOfForums.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(final int groupPosition, final boolean isExpanded, View convertView, final ViewGroup parent) {
        ViewHolder holder;
        String groupTitle = (String) getGroup(groupPosition);

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = serviceInflater.inflate(R.layout.jvcforums_row, parent, false);
            holder.itemOne = (TextView) convertView.findViewById(R.id.item_one_jvcforums_text_row);
            holder.imageButtonExpand = (ImageView) convertView.findViewById(R.id.image_button_expand_jvcforums_text_row);
            holder.isForumImage = (ImageView) convertView.findViewById(R.id.image_is_forum_jvcforums_text_row);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        convertView.setBackgroundColor(Undeprecator.resourcesGetColor(parentActivity.getResources(), R.color.defaultColorForBackground));
        holder.itemOne.setText(groupTitle);
        holder.imageButtonExpand.setImageResource(isExpanded ? R.drawable.ic_action_navigation_expand_less_dark : R.drawable.ic_action_navigation_expand_more_dark);

        if (currentListOfForums.get(groupPosition).isTitle) {
            holder.itemOne.setTypeface(null, Typeface.ITALIC);
            holder.itemOne.setPaintFlags(holder.itemOne.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            holder.imageButtonExpand.setVisibility(View.INVISIBLE);
        } else {
            holder.itemOne.setTypeface(null, Typeface.BOLD);
            holder.itemOne.setPaintFlags(holder.itemOne.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));
            if (getChildrenCount(groupPosition) > 0) {
                holder.imageButtonExpand.setVisibility(View.VISIBLE);
            } else {
                holder.imageButtonExpand.setVisibility(View.INVISIBLE);
            }
        }

        if (currentListOfForums.get(groupPosition).isForum) {
            holder.isForumImage.setVisibility(View.VISIBLE);
        } else {
            holder.isForumImage.setVisibility(View.GONE);
        }


        holder.imageButtonExpand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isExpanded) {
                    ((ExpandableListView) parent).collapseGroup(groupPosition);
                } else {
                    ((ExpandableListView) parent).expandGroup(groupPosition, true);
                }
            }
        });

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    @Override
    public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
        ForumInfo currentItem = currentListOfForums.get(groupPosition);

        if (!currentItem.isForum && !currentItem.isTitle) {
            if (parent.isGroupExpanded(groupPosition)) {
                parent.collapseGroup(groupPosition);
            } else {
                parent.expandGroup(groupPosition, true);
            }
        } else if (currentItem.isForum) {
            if (!currentItem.forum.link.isEmpty()) {
                if (parentActivity instanceof NewForumSelected) {
                    ((NewForumSelected) parentActivity).getNewForumLink(currentItem.forum.link);
                }
            }
        }
        return true;
    }

    @Override
    public boolean onChildClick(ExpandableListView expandableListView, View view, int groupPosition, int childPosition, long l) {
        JVCParser.NameAndLink currentItem = currentListOfForums.get(groupPosition).subForumList.get(childPosition);

        if (!currentItem.link.isEmpty()) {
            if (parentActivity instanceof NewForumSelected) {
                ((NewForumSelected) parentActivity).getNewForumLink(currentItem.link);
            }
        }
        return true;
    }

    private class ForumInfo {
        boolean isTitle = false;
        boolean isForum = true;
        JVCParser.NameAndLink forum = null;
        ArrayList<JVCParser.NameAndLink> subForumList = new ArrayList<>();
    }

    private class ViewHolder {
        private TextView itemOne;
        private ImageView imageButtonExpand;
        private ImageView isForumImage;
    }

    public interface NewForumSelected {
        void getNewForumLink(String link);
    }
}
