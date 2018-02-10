package com.pokemonshowdown.data;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pokemonshowdown.app.BattleFragment;
import com.pokemonshowdown.app.ChatRoomFragment;
import com.pokemonshowdown.app.R;
import com.pokemonshowdown.application.MyApplication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import static com.pokemonshowdown.data.Translations.translateAbility;
import static com.pokemonshowdown.data.Translations.translateMove;
import static com.pokemonshowdown.data.Translations.translatePokemon;

public class BattleMessage {

    public static void processMajorAction(final BattleFragment battleFragment, final String message)
            throws JSONException {
        BattleFieldData.RoomData roomData = BattleFieldData.get(battleFragment.getActivity()).getAnimationInstance(battleFragment.getRoomId());
        final BattleFieldData.ViewData viewData = BattleFieldData.get(battleFragment.getActivity()).getViewData(battleFragment.getRoomId());
        String command = (message.indexOf('|') == -1) ? message : message.substring(0, message.indexOf('|'));
        final String messageDetails = message.substring(message.indexOf('|') + 1);
        if (command.startsWith("-")) {
            processMinorAction(battleFragment, command, messageDetails, message);
            return;
        }

        int separator = messageDetails.indexOf('|');
        final String[] split = messageDetails.split("\\|");
        final ArrayList<PokemonInfo> team1 = battleFragment.getPlayer1Team();
        final ArrayList<PokemonInfo> team2 = battleFragment.getPlayer2Team();

        final ArrayList<PokemonInfo> team;
        final String position, attacker;
        final int iconId;
        final PokemonInfo pokemonInfo;
        int start;
        String remaining;
        final String toAppend;
        StringBuilder toAppendBuilder;
        Spannable toAppendSpannable;
        AnimatorSet toast;
        AnimatorSet animatorSet;
        Animator animator;

        Spannable logMessage = new SpannableString("");
        switch (command) {
            case "deinit":
                return;
            case "askreg":
                toast = battleFragment.makeToast(battleFragment.getResources().getString(R.string.ask_registration));
                battleFragment.startAnimation(toast, message);
                logMessage = new SpannableString(messageDetails);
                break;
            case "title":
            case "J":
            case "L":
            case "spectator":
            case "spectatorLeave":
                break;

            case "join":
            case "j":
                break;

            case "leave":
            case "l":
                break;

            case "chat":
            case "c":
            case "tc":
            case "c:":
                String user = split[0];
                String userMessage = split[1];
                toAppend = user + ": " + userMessage;
                toAppendSpannable = new SpannableString(toAppend);
                toAppendSpannable.setSpan(new ForegroundColorSpan(ChatRoomFragment.getColorStrong(user)),
                        0, user.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                logMessage = new SpannableString(toAppendSpannable);
                battleFragment.makeChatToast(user, userMessage);
                break;

            case "raw":
            case "variation":
            case "chatmsg":
            case "chatmsg-raw":
            case "html":
                toast = battleFragment.makeToast(Html.fromHtml(messageDetails).toString());
                battleFragment.startAnimation(toast, message);
                logMessage = new SpannableString(Html.fromHtml(messageDetails).toString());
                break;

            case "message":
                toast = battleFragment.makeToast(messageDetails);
                battleFragment.startAnimation(toast, message);
                logMessage = new SpannableString(messageDetails);
                break;

            case "gametype":
                if("singles".equals(messageDetails)) {
                    battleFragment.setSingles(true);
                } else {
                    battleFragment.setSingles(false);
                }
                break;
            case "gen":
                break;

            case "player":
                final String playerType;
                final String playerName;
                String avatar;
                if (separator == -1) {
                    playerType = messageDetails;
                    playerName = "";
                    avatar = null;
                } else {
                    playerType = split[0];
                    playerName = split[1];
                    avatar = split[2];
                    while (avatar.length() < 3) {
                        avatar = "0" + avatar;
                    }
                }
                final int avatarResource;
                if (avatar != null) {
                    avatarResource = battleFragment.getActivity().getApplicationContext()
                            .getResources().getIdentifier("avatar_" + avatar, "drawable",
                                    battleFragment.getActivity().getApplicationContext().getPackageName());

                } else {
                    avatarResource = 0;
                }
                if (playerType.equals("p1")) {
                    roomData.setPlayer1(playerName);
                    battleFragment.getActivity().runOnUiThread(new RunWithNet() {
                        @Override
                        public void runWithNet() {
                            if (battleFragment.getView() == null) {
                                viewData.addViewSetterOnHold(R.id.username, playerName,
                                        BattleFieldData.ViewData.SetterType.TEXTVIEW_SETTEXT);
                                viewData.addViewSetterOnHold(R.id.avatar, avatarResource,
                                        BattleFieldData.ViewData.SetterType.IMAGEVIEW_SETIMAGERESOURCE);
                            } else {
                                ((TextView) battleFragment.getView().findViewById(R.id.username)).setText(playerName);
                                ((ImageView) battleFragment.getView().findViewById(R.id.avatar)).setImageResource(avatarResource);
                            }
                        }
                    });
                    battleFragment.setPlayer1(playerName);
                } else {
                    roomData.setPlayer2(playerName);
                    battleFragment.getActivity().runOnUiThread(new RunWithNet() {
                        @Override
                        public void runWithNet() {
                            if (battleFragment.getView() == null) {
                                viewData.addViewSetterOnHold(R.id.username_o, playerName,
                                        BattleFieldData.ViewData.SetterType.TEXTVIEW_SETTEXT);
                                viewData.addViewSetterOnHold(R.id.avatar_o, avatarResource,
                                        BattleFieldData.ViewData.SetterType.IMAGEVIEW_SETIMAGERESOURCE);
                            } else {
                                ((TextView) battleFragment.getView().findViewById(R.id.username_o)).setText(playerName);
                                ((ImageView) battleFragment.getView().findViewById(R.id.avatar_o)).setImageResource(avatarResource);
                            }
                        }
                    });
                    battleFragment.setPlayer2(playerName);
                }
                break;

            case "tier":
                toAppend = "Format:" + "\n" + messageDetails;
                toAppendSpannable = new SpannableString(toAppend);
                toAppendSpannable.setSpan(new StyleSpan(Typeface.BOLD),
                        toAppend.indexOf('\n') + 1, toAppend.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                logMessage = new SpannableString(toAppendSpannable);
                break;

            case "rated":
                toAppend = command.toUpperCase();
                toAppendSpannable = new SpannableString(toAppend);
                toAppendSpannable.setSpan(new ForegroundColorSpan(R.color.dark_blue),
                        0, toAppend.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                logMessage = new SpannableString(toAppendSpannable);
                break;

            case "rule":
                toAppendSpannable = new SpannableString(messageDetails);
                toAppendSpannable.setSpan(new StyleSpan(Typeface.ITALIC),
                        0, messageDetails.indexOf(':') + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                logMessage = new SpannableString(toAppendSpannable);
                break;

            case "choice":
            case "":
                logMessage = new SpannableString(" ");
                break;

            case "clearpoke":
                if (battleFragment.getPlayer1Team() == null) {
                    battleFragment.setPlayer1Team(new ArrayList<PokemonInfo>());
                }
                battleFragment.setPlayer2Team(new ArrayList<PokemonInfo>());
                break;

            case "poke":
                playerType = split[0];
                int comma = split[1].indexOf(',');
                final String pokeName = (comma == -1) ? split[1] : split[1].substring(0, comma);
                team = (playerType.equals("p1")) ? team1 : team2;
                iconId = battleFragment.getIconId(playerType, team.size());
                pokemonInfo = new PokemonInfo(battleFragment.getActivity(), pokeName);
                processPokemonDetailString(pokemonInfo, split[1]);
                if (battleFragment.findPokemonInTeam(team,
                        pokemonInfo.getName()) == -1) {
                    team.add(pokemonInfo);

                    battleFragment.getActivity().runOnUiThread(new RunWithNet() {
                        @Override
                        public void runWithNet() {
                            int imageResource = Pokemon.getPokemonIcon(battleFragment.getActivity(),
                                    MyApplication.toId(pokeName));
                            if (battleFragment.getView() == null) {
                                viewData.addViewSetterOnHold(iconId, imageResource,
                                        BattleFieldData.ViewData.SetterType.IMAGEVIEW_SETIMAGERESOURCE);
                            } else {
                                ImageView icon = (ImageView) battleFragment.getView().findViewById(iconId);
                                if (icon != null) {
                                    icon.setImageResource(imageResource);
                                }
                            }
                        }
                    });
                }
                break;

            case "teampreview":
                int teamSelectionSize;
                try {
                    teamSelectionSize = Integer.parseInt(messageDetails);
                    // sometimes messageDetails is the number of mons (for VGC), sometimes it's just teampreview
                } catch (NumberFormatException e) {
                    teamSelectionSize = 0;
                }

                final ArrayList<PokemonInfo> t1 = battleFragment.getPlayer1Team();
                final ArrayList<PokemonInfo> t2 = battleFragment.getPlayer2Team();

                final int teamSelectionSizeFinal = teamSelectionSize;
                battleFragment.getActivity().runOnUiThread(new RunWithNet() {
                    @Override
                    public void runWithNet() {
                        if (battleFragment.getView() == null) {
                            return;
                        }

                        FrameLayout frameLayout = (FrameLayout) battleFragment.getView().findViewById(R.id.battle_interface);
                        frameLayout.removeAllViews();
                        battleFragment.getActivity().getLayoutInflater().inflate(R.layout.fragment_battle_teampreview, frameLayout);
                        for (int i = 0; i < t1.size(); i++) {
                            ImageView sprites = (ImageView) battleFragment.getView().findViewById(battleFragment.getTeamPreviewSpriteId("p1", i));
                            PokemonInfo pkm = t1.get(i);
                            sprites.setImageResource(Pokemon.getPokemonSprite(battleFragment.getActivity(),
                                    MyApplication.toId(pkm.getName()), true, pkm.isFemale(), pkm.isShiny()));
                            ((ImageView) battleFragment.getView().findViewById(battleFragment.getIconId("p1", i)))
                                    .setImageResource(Pokemon.getPokemonIcon(battleFragment.getActivity(),
                                            MyApplication.toId(pkm.getName())));
                        }
                        for (int i = t1.size(); i < 6; i++) {
                            ((ImageView) battleFragment.getView().findViewById(battleFragment.getIconId("p1", i)))
                                    .setImageResource(R.drawable.pokeball_none);
                        }
                        for (int i = 0; i < t2.size(); i++) {
                            ImageView sprites = (ImageView) battleFragment.getView().findViewById(battleFragment.getTeamPreviewSpriteId("p2", i));
                            PokemonInfo pkm = t2.get(i);
                            sprites.setImageResource(Pokemon.getPokemonSprite(battleFragment.getActivity(),
                                    MyApplication.toId(pkm.getName()), false, pkm.isFemale(), pkm.isShiny()));
                            ((ImageView) battleFragment.getView().findViewById(battleFragment.getIconId("p2", i)))
                                    .setImageResource(Pokemon.getPokemonIcon(battleFragment.getActivity(),
                                            MyApplication.toId(pkm.getName())));
                        }
                        for (int i = t2.size(); i < 6; i++) {
                            ((ImageView) battleFragment.getView().findViewById(battleFragment.getIconId("p2", i)))
                                    .setImageResource(R.drawable.pokeball_none);
                        }
                        battleFragment.setTeamSize(teamSelectionSizeFinal);
                        battleFragment.getView().findViewById(R.id.p2a_prev)
                                .setOnClickListener(battleFragment.new PokemonInfoListener(false, 0));
                        battleFragment.getView().findViewById(R.id.p2b_prev)
                                .setOnClickListener(battleFragment.new PokemonInfoListener(false, 1));
                        battleFragment.getView().findViewById(R.id.p2c_prev)
                                .setOnClickListener(battleFragment.new PokemonInfoListener(false, 2));
                        battleFragment.getView().findViewById(R.id.p2d_prev)
                                .setOnClickListener(battleFragment.new PokemonInfoListener(false, 3));
                        battleFragment.getView().findViewById(R.id.p2e_prev)
                                .setOnClickListener(battleFragment.new PokemonInfoListener(false, 4));
                        battleFragment.getView().findViewById(R.id.p2f_prev)
                                .setOnClickListener(battleFragment.new PokemonInfoListener(false, 5));

                        if (battleFragment.getAnimatorSetQueue().isEmpty() && battleFragment.getRequestJson() != null) {
                            battleFragment.startRequest();
                        }

                    }
                });

                toAppendBuilder = new StringBuilder();
                toAppendBuilder.append(battleFragment.getPlayer1()).append("'s Team: ");
                String[] p1Team = battleFragment.getTeamNameStringArray(team1);
                for (int i = 0; i < p1Team.length - 1; i++) {
                    toAppendBuilder.append(p1Team[i]).append("/");
                }
                toAppendBuilder.append(p1Team[p1Team.length - 1]);

                toAppendBuilder.append("\n").append(battleFragment.getPlayer2()).append("'s Team: ");
                String[] p2Team = battleFragment.getTeamNameStringArray(team2);
                for (int i = 0; i < p2Team.length - 1; i++) {
                    toAppendBuilder.append(p2Team[i]).append("/");
                }
                toAppendBuilder.append(p2Team[p2Team.length - 1]);
                toAppendSpannable = new SpannableStringBuilder(toAppendBuilder);
                logMessage = new SpannableString(toAppendSpannable);
                break;

            case "request":
                if (messageDetails.equals("null")) {
                    return;
                }
                JSONObject requestJson = new JSONObject(messageDetails);

                if (battleFragment.getBattling() == 0) {
                    battleFragment.setBattling(1);
                    battleFragment.setUpTimer();
                }

                battleFragment.setRequestJson(requestJson);
                battleFragment.setUndoMessage(requestJson);
                break;

            case "inactive":
                final String inactive;
                if (messageDetails.contains(" seconds left")) {
                    remaining = messageDetails.substring(0, messageDetails.indexOf(" seconds left"));
                    inactive = remaining.substring(remaining.lastIndexOf(' ')) + "s";

                    battleFragment.getActivity().runOnUiThread(new RunWithNet() {
                        @Override
                        public void runWithNet() {
                            if (battleFragment.getView() == null) {
                                viewData.addViewSetterOnHold(R.id.inactive, inactive,
                                        BattleFieldData.ViewData.SetterType.TEXTVIEW_SETTEXT);
                                viewData.addViewSetterOnHold(R.id.inactive, null,
                                        BattleFieldData.ViewData.SetterType.VIEW_VISIBLE);
                            } else {
                                TextView textView = (TextView) battleFragment.getView().findViewById(R.id.inactive);
                                textView.setVisibility(View.VISIBLE);
                                textView.setText(inactive);
                            }
                        }
                    });
                }
                toAppendSpannable = new SpannableString(messageDetails);
                toAppendSpannable.setSpan(new ForegroundColorSpan(R.color.dark_red),
                        0, messageDetails.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                logMessage = new SpannableString(toAppendSpannable);
                break;

            case "inactiveoff":
                battleFragment.getActivity().runOnUiThread(new RunWithNet() {
                    @Override
                    public void runWithNet() {
                        if (battleFragment.getView() == null) {
                            viewData.addViewSetterOnHold(R.id.inactive, null,
                                    BattleFieldData.ViewData.SetterType.VIEW_GONE);
                        } else {
                            battleFragment.getView().findViewById(R.id.inactive).setVisibility(View.GONE);
                        }
                    }
                });
                toAppendSpannable = new SpannableString(messageDetails);
                toAppendSpannable.setSpan(new ForegroundColorSpan(R.color.dark_red),
                        0, messageDetails.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                logMessage = new SpannableString(toAppendSpannable);
                break;

            case "start":
                if (Onboarding.get(battleFragment.getActivity()).isAdvertising()) {
                    // sending advertisement message
                    String advertisement = battleFragment.getRoomId() + "|" + battleFragment.getActivity().getString(R.string.advertise_message);
                    MyApplication.getMyApplication().sendClientMessage(advertisement);
                }
                battleFragment.getActivity().runOnUiThread(new RunWithNet() {
                    @Override
                    public void runWithNet() {
                        if (battleFragment.getView() == null) {
                            viewData.addViewSetterOnHold(R.id.battle_interface, null,
                                    BattleFieldData.ViewData.SetterType.BATTLE_START);
                        } else {
                            FrameLayout frameLayout = (FrameLayout) battleFragment.getView().findViewById(R.id.battle_interface);
                            frameLayout.removeAllViews();
                            battleFragment.getActivity().getLayoutInflater()
                                    .inflate(R.layout.fragment_battle_animation, frameLayout);
                            battleFragment.getView().findViewById(R.id.p1a)
                                    .setOnClickListener(battleFragment.new PokemonInfoListener(true, 0));
                            battleFragment.getView().findViewById(R.id.p1b)
                                    .setOnClickListener(battleFragment.new PokemonInfoListener(true, 1));
                            battleFragment.getView().findViewById(R.id.p1c)
                                    .setOnClickListener(battleFragment.new PokemonInfoListener(true, 2));
                            battleFragment.getView().findViewById(R.id.p2a)
                                    .setOnClickListener(battleFragment.new PokemonInfoListener(false, 0));
                            battleFragment.getView().findViewById(R.id.p2b)
                                    .setOnClickListener(battleFragment.new PokemonInfoListener(false, 1));
                            battleFragment.getView().findViewById(R.id.p2c)
                                    .setOnClickListener(battleFragment.new PokemonInfoListener(false, 2));
                        }
                    }
                });
                toAppend = battleFragment.getPlayer1() + " vs. " + battleFragment.getPlayer2();
                toAppendSpannable = new SpannableString(toAppend);
                toAppendSpannable.setSpan(new StyleSpan(Typeface.BOLD),
                        0, toAppend.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                logMessage = new SpannableString(toAppendSpannable);
                break;

            case "move":
                attacker = split[0].substring(5);
                toAppendBuilder = new StringBuilder();
                if (messageDetails.startsWith("p2")) {
                    toAppendBuilder.append("The opposing's ");
                }
                toAppendBuilder.append(attacker).append(" used ");
                final String move = MyApplication.toId(split[1]);
                if (move.equals("batonpass")) {
                    battleFragment.setBatonPass(true);
                }
                toAppendBuilder.append(split[1]).append("!");
                toAppend = toAppendBuilder.toString();
                start = toAppend.indexOf(split[1]);
                toAppendSpannable = new SpannableString(toAppend);
                toAppendSpannable.setSpan(new StyleSpan(Typeface.BOLD),
                        start, start + split[1].length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                logMessage = toAppendSpannable;
                toast = battleFragment.makeToast(logMessage);

                toast.addListener(new AnimatorListenerWithNet() {
                    @Override
                    public void onAnimationStartWithNet(Animator animation) {
                        if (battleFragment.getView() == null) {
                            return;
                        }

                        PokemonInfo pokemonInfo = battleFragment.getPokemonInfo(split[0]);
                        if (!move.equals("struggle")) {
                            HashMap<String, Integer> moves = pokemonInfo.getMoves();
                            if (moves.containsKey(move)) {
                                moves.put(move, moves.get(move) - 1);
                            } else {
                                moves.put(move, Integer.parseInt(MoveDex.getMoveMaxPP(battleFragment.getActivity(), move)) - 1);
                            }
                        }

                        if (!messageDetails.contains("[still]")) {
                            AnimatorSet animatorSet = BattleAnimation.processMove(move, battleFragment, split);
                            if (animatorSet != null && Onboarding.get(battleFragment.getActivity()).isAnimation()) {
                                battleFragment.setCurrentBattleAnimation(animatorSet);
                                animatorSet.addListener(new AnimatorListenerWithNet() {
                                    @Override
                                    public void onAnimationEndWithNet(Animator animation) {
                                        super.onAnimationEndWithNet(animation);
                                        battleFragment.setCurrentBattleAnimation(null);
                                    }
                                });
                                animatorSet.start();
                            }
                        }
                    }
                });
                battleFragment.startAnimation(toast, message);
                break;

            case "switch":
            case "drag":
            case "replace":
                final int toBeSwapped;

                //TODO need to handle roar & cie
                toAppendBuilder = new StringBuilder();

                int tempHp;
                String tempStatus;

                try {
                    tempHp = processHpFraction(split[2]);
                    tempStatus = processStatusFraction(split[2]);
                } catch (ArrayIndexOutOfBoundsException ex) { // zoroark's transform
                    tempHp = processHpFraction(split[0]);
                    tempStatus = processStatusFraction(split[0]);
                }

                final int hp = processHpFraction(split[2]);
                final String status = processStatusFraction(split[2]);

                String species = !split[1].contains(",") ? split[1] :
                        split[1].substring(0, split[1].indexOf(","));
                String speciesId = MyApplication.toId(species);
                pokemonInfo = new PokemonInfo(battleFragment.getActivity(), speciesId);
                processPokemonDetailString(pokemonInfo, split[1]);
                pokemonInfo.setHp(hp);
                pokemonInfo.setStatus(status);

                // Switching sprites and icons
                ArrayList<PokemonInfo> playerTeam = battleFragment.getTeam(split[0]);
                if (playerTeam == null) {
                    playerTeam = new ArrayList<>();
                }
                if (battleFragment.findPokemonInTeam(playerTeam, species) == -1) {
                    playerTeam.add(playerTeam.size(), pokemonInfo);
                    toBeSwapped = playerTeam.size() - 1;
                } else {
                    toBeSwapped = battleFragment.findPokemonInTeam(playerTeam, species);
                }
                int j = battleFragment.getTeamSlot(messageDetails);
                PokemonInfo holder = playerTeam.get(j);
                playerTeam.set(j, playerTeam.get(toBeSwapped));
                playerTeam.set(toBeSwapped, holder);

                battleFragment.setTeam(messageDetails, playerTeam);

                if (command.equals("switch")) {
                    if (messageDetails.startsWith("p2")) {
                        toAppendBuilder.append(battleFragment.getPlayer2()).append(" sent out ").append(species).append("!");
                    } else {
                        toAppendBuilder.append("Go! ").append(species).append("!");
                    }
                } else {
                    if (command.equals("drag")) {
                        toAppendBuilder.append(species).append(" was dragged out!");
                    }
                }

                toast = battleFragment.makeToast(new SpannableStringBuilder(toAppendBuilder));
                toast.addListener(new AnimatorListenerWithNet() {
                    @Override
                    public void onAnimationStartWithNet(Animator animation) {
                        if (battleFragment.getView() == null) {
                            return;
                        }

                        battleFragment.displayPokemon(split[0]);

                        ImageView sprites = (ImageView) battleFragment.getView()
                                .findViewById(battleFragment.getSpriteId(split[0]));
                        if (sprites != null) {
                            sprites.setImageResource(pokemonInfo.getSprite(battleFragment.getActivity()));
                        }
                        ImageView iconLeader = (ImageView) battleFragment.getView()
                                .findViewById(battleFragment.getIconId(split[0], battleFragment.getTeamSlot(messageDetails)));
                        Drawable leader = iconLeader.getDrawable();
                        ImageView iconTrailer = (ImageView) battleFragment.getView()
                                .findViewById(battleFragment.getIconId(split[0], toBeSwapped));
                        float alphaTrailer = iconTrailer.getAlpha();
                        iconTrailer.setImageDrawable(leader);
                        iconTrailer.setAlpha(iconLeader.getAlpha());
                        iconLeader.setImageResource(pokemonInfo.getIcon(battleFragment.getActivity()));
                        iconLeader.setAlpha(alphaTrailer);

                        TextView pkmName = (TextView) battleFragment.getView()
                                .findViewById(battleFragment.getSpriteNameid(split[0]));
                        if (pkmName != null) {
                            if (pokemonInfo.getLevel() != 100) {
                                pkmName.setText(pokemonInfo.getNickname() + " L" + pokemonInfo.getLevel() + " ");
                            } else {
                                pkmName.setText(pokemonInfo.getNickname() + " ");
                            }
                        }

                        ImageView gender = (ImageView) battleFragment.getView()
                                .findViewById(battleFragment.getGenderId(split[0]));
                        gender.setImageResource(getGenderSprite(pokemonInfo.getGender()));

                        TextView hpText = (TextView) battleFragment.getView()
                                .findViewById(battleFragment.getHpId(messageDetails.substring(0, 3)));
                        ProgressBar hpBar = (ProgressBar) battleFragment.getView()
                                .findViewById(battleFragment.getHpBarId(messageDetails.substring(0, 3)));
                        if (hpText != null) {
                            hpText.setText(Integer.toString(hp));
                        }
                        if (hpBar != null) {
                            hpBar.setProgress(hp);
                        }

                        if (status != null) {
                            battleFragment.setAddonStatus(split[0], status.toLowerCase());
                        }
                    }
                });
                logMessage = new SpannableString(toAppendBuilder);
                battleFragment.startAnimation(toast, message);
                break;

            case "detailschange":
                final String forme = (split[1].contains(",")) ? split[1].substring(0, split[1].indexOf(',')) : split[1];

                position = split[0].substring(0, 3);

                battleFragment.formChange(position, forme);

                pokemonInfo = battleFragment.getPokemonInfo(position);

                toast = battleFragment.makeToast("Transforming", BattleFragment.ANIMATION_SHORT);
                toast.addListener(new AnimatorListenerWithNet() {
                    @Override
                    public void onAnimationStartWithNet(Animator animation) {
                        if (battleFragment.getView() == null) {
                            return;
                        }

                        boolean back = split[0].startsWith("p1");
                        ImageView sprite = (ImageView) battleFragment.getView().findViewById(battleFragment.getSpriteId(position));
                        sprite.setImageResource(Pokemon.getPokemonSprite(battleFragment.getActivity(),
                                MyApplication.toId(forme), back, pokemonInfo.isFemale(), pokemonInfo.isShiny()));
                        ImageView icon = (ImageView) battleFragment.getView().findViewById(battleFragment.getIconId(position));
                        icon.setImageResource(Pokemon.getPokemonIcon(battleFragment.getActivity(),
                                MyApplication.toId(forme)));
                    }
                });
                battleFragment.startAnimation(toast, message);
                break;

            case "faint":
                position = split[0];
                attacker = split[0].substring(5);
                toAppendBuilder = new StringBuilder();
                if (messageDetails.startsWith("p2")) {
                    toAppendBuilder.append("The opposing ");
                }
                toAppendBuilder.append(attacker).append(" fainted!");
                toast = battleFragment.makeToast(new SpannableStringBuilder(toAppendBuilder));
                toast.addListener(new AnimatorListenerWithNet() {
                    @Override
                    public void onAnimationStartWithNet(Animator animation) {
                        if (battleFragment.getView() == null) {
                            return;
                        }

                        PokemonInfo faintedPokemon = battleFragment.getPokemonInfo(split[0]);
                        faintedPokemon.setHp(0);
                        battleFragment.hidePokemon(position);
                        battleFragment.getView().findViewById(battleFragment.getIconId(position)).setAlpha(0.5f);
                    }
                });

                battleFragment.startAnimation(toast, message);
                logMessage = new SpannableString(toAppendBuilder);
                break;

            case "turn":
                if (battleFragment.getView() == null) {
                    return;
                }
                TextView turn = (TextView) battleFragment.getView().findViewById(R.id.turn);
                animator = ObjectAnimator.ofFloat(turn, "alpha", 0f, 1f);
                toAppend = "TURN " + messageDetails;
                animator.addListener(new AnimatorListenerWithNet() {
                    @Override
                    public void onAnimationStartWithNet(Animator animation) {
                        if (battleFragment.getView() == null) {
                            return;
                        }
                        battleFragment.getView().findViewById(R.id.turn).setVisibility(View.VISIBLE);
                        ((TextView) battleFragment.getView().findViewById(R.id.turn)).setText(toAppend);
                        (battleFragment.getView().findViewById(R.id.inactive)).setVisibility(View.GONE);
                    }
                });
                animator.setDuration(BattleFragment.ANIMATION_SHORT);
                animator.setInterpolator(new AccelerateDecelerateInterpolator());
                animatorSet = new AnimatorSet();
                animatorSet.play(animator);
                battleFragment.startAnimation(animatorSet, message);
                toAppendSpannable = new SpannableString(toAppend.toUpperCase());
                toAppendSpannable.setSpan(new UnderlineSpan(), 0, toAppend.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                toAppendSpannable.setSpan(new StyleSpan(Typeface.BOLD), 0, toAppend.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                toAppendSpannable.setSpan(new RelativeSizeSpan(1.25f), 0, toAppend.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                toAppendSpannable.setSpan(new ForegroundColorSpan(R.color.dark_blue), 0, toAppend.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                logMessage = new SpannableString(toAppendSpannable);
                break;

            case "win":
                toAppend = messageDetails + " has won the battle!";
                toast = battleFragment.makeToast(new SpannableString(toAppend));
                battleFragment.startAnimation(toast, message);
                logMessage = new SpannableString(toAppend);

                if (battleFragment.getBattling() != 0) {
                    battleFragment.showEndBattleDialog(toAppend);
                }
                break;

            case "tie":
                toAppend = "The battle is a tie!";
                toast = battleFragment.makeToast(new SpannableString(toAppend));
                battleFragment.startAnimation(toast, message);
                logMessage = new SpannableString(toAppend);
                if (battleFragment.getBattling() != 0) {
                    battleFragment.showEndBattleDialog(toAppend);
                }
                break;

            case "cant":
                String attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0]);
                toAppendBuilder = new StringBuilder();
                switch (MyApplication.toId(battleFragment.getPrintable(split[1]))) {
                    case "taunt":
                        toAppendBuilder.append(attackerOutputName).append(" can't use ").append(battleFragment.getPrintable(split[2])).append(" after the taunt!");
                        break;

                    case "gravity":
                        toAppendBuilder.append(attackerOutputName).append(" can't use ").append(battleFragment.getPrintable(split[2])).append(" because of gravity!");
                        break;

                    case "healblock":
                        toAppendBuilder.append(attackerOutputName).append(" can't use ").append(battleFragment.getPrintable(split[2])).append(" because of Heal Block!");
                        break;

                    case "imprison":
                        toAppendBuilder.append(attackerOutputName).append(" can't use the sealed ").append(battleFragment.getPrintable(split[2])).append("!");
                        break;

                    case "par":
                        toAppendBuilder.append(attackerOutputName).append(" is paralyzed! It can't move!");
                        break;

                    case "frz":
                        toAppendBuilder.append(attackerOutputName).append(" is frozen solid!'");
                        break;

                    case "slp":
                        toAppendBuilder.append(attackerOutputName).append(" is fast asleep.");
                        break;

                    case "skydrop":
                        attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0], false);
                        toAppendBuilder.append("Sky Drop won't let ").append(attackerOutputName);
                        break;

                    case "truant":
                        toAppendBuilder.append(attackerOutputName).append(" is loafing around!");
                        break;

                    case "recharge":
                        toAppendBuilder.append(attackerOutputName).append(" must recharge!");
                        break;

                    case "focuspunch":
                        toAppendBuilder.append(attackerOutputName).append(" lost its focus and couldn't move!");
                        break;

                    case "flinch":
                        toAppendBuilder.append(attackerOutputName).append(" flinched and couldn't move!");
                        break;

                    case "attract":
                        toAppendBuilder.append(attackerOutputName).append(" is immobilized by love!");
                        break;

                    case "nopp":
                        toAppendBuilder.append(attackerOutputName).append(" used ").append(battleFragment.getPrintable(split[2]));
                        toAppendBuilder.append("\nBut there was no PP left for the move!");
                        break;

                    default:
                        toAppendBuilder.append(attackerOutputName);
                        if (split.length > 2) {
                            toAppendBuilder.append(" can't use ").append(battleFragment.getPrintable(split[2]));
                        } else {
                            toAppendBuilder.append(" can't move");
                        }
                        toAppendBuilder.append("!");
                        break;
                }
                logMessage = new SpannableString(toAppendBuilder);
                toast = battleFragment.makeMinorToast(logMessage);
                animatorSet = battleFragment.createFlyingMessage(split[0], toast, new SpannableString("Failed!"));
                battleFragment.startAnimation(animatorSet, message);
                break;

            default:
                /*toast = battleFragment.makeToast(message, BattleFragment.ANIMATION_LONG);
                battleFragment.startAnimation(toast, message);*/
                logMessage = new SpannableString(message);
                break;
        }

        battleFragment.addToLog(logMessage);
    }


    public static void processMinorAction(final BattleFragment battleFragment, String command, final String messageDetails, final String message) {
        int separator;
        final PokemonInfo pokemonInfo;
        Integer oldHP;
        final int lostHP;
        final int intAmount;
        String remaining;
        String toAppend;
        StringBuilder toAppendBuilder = new StringBuilder();
        Spannable toAppendSpannable;
        Spannable logMessage = new SpannableString("");
        String move, ability;
        boolean flag, eat, weaken;

        String fromEffect = null;
        String fromEffectId = null;
        String ofSource = null;
        String trimmedOfEffect = null;

        String attacker, defender, side, stat, statAmount;
        String attackerOutputName;
        String defenderOutputName;

        int from = messageDetails.indexOf("[from]");
        if (from != -1) {
            remaining = messageDetails.substring(from + 7);
            separator = remaining.indexOf('|');
            fromEffect = (separator == -1) ? remaining : remaining.substring(0, separator);
            //trim
            fromEffectId = MyApplication.toId(fromEffect);
        }
        int of = messageDetails.indexOf("[of]");
        if (of != -1) {
            remaining = messageDetails.substring(of + 5);
            separator = remaining.indexOf('|');
            ofSource = (separator == -1) ? remaining : remaining.substring(remaining.indexOf(':'), separator);

            trimmedOfEffect = MyApplication.toId(ofSource);
        }

        final String[] split = messageDetails.split("\\|");

        AnimatorSet toast;
        AnimatorSet animatorSet;

        if (battleFragment.getView() == null) {
            return;
        }

        switch (command) {
            case "-damage":
                attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0]);
                pokemonInfo = battleFragment.getPokemonInfo(messageDetails);
                oldHP = pokemonInfo.getHp();
                intAmount = processHpFraction(split[1]);
                pokemonInfo.setHp(intAmount);
                lostHP = intAmount - oldHP;

                if (fromEffectId != null) {
                    switch (battleFragment.trimOrigin(fromEffect)) {
                        case "stealthrock":
                            attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0], false);
                            toAppendBuilder.append("Pointed stones dug into ").append(attackerOutputName).append("!");
                            break;
                        case "spikes":
                            toAppendBuilder.append(attackerOutputName).append(" is hurt by the spikes!");
                            break;
                        case "brn":
                            toAppendBuilder.append(attackerOutputName).append(" was hurt by its burn!");
                            break;
                        case "psn":
                            toAppendBuilder.append(attackerOutputName).append(" was hurt by poison!");
                            break;
                        case "lifeorb":
                            toAppendBuilder.append(attackerOutputName).append(" lost some of its HP!");
                            pokemonInfo.setItem("lifeorb");
                            break;
                        case "recoil":
                            toAppendBuilder.append(attackerOutputName).append(" is damaged by recoil!");
                            break;
                        case "ironbarbs":
                            PokemonInfo barbTarget = battleFragment.getPokemonInfo(split[3]);
                            if (barbTarget != null) {
                                barbTarget.setAbility("ironbarbs");
                            }
                            toAppendBuilder.append(attackerOutputName).append(" is hurt by ").append(battleFragment.getPrintable(ofSource)).append("'s ").append(" Iron Barbs!");
                            break;
                        case "sandstorm":
                            toAppendBuilder.append(attackerOutputName).append(" is buffeted by the sandstorm!");
                            break;
                        case "hail":
                            toAppendBuilder.append(attackerOutputName).append(" is buffeted by the hail!");
                            break;
                        case "baddreams":
                            toAppendBuilder.append(attackerOutputName).append(" is tormented!");
                            break;
                        case "nightmare":
                            toAppendBuilder.append(attackerOutputName).append(" is locked in a nightmare!");
                            break;
                        case "confusion":
                            toAppendBuilder.append("It hurt itself in its confusion!");
                            break;
                        case "leechseed":
                            toAppendBuilder.append(attackerOutputName).append("'s health is sapped by Leech Seed!");
                            break;
                        case "flameburst":
                            attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0], false);
                            toAppendBuilder.append("The bursting flame hit ").append(attackerOutputName).append("!");
                            break;
                        case "firepledge":
                            toAppendBuilder.append(attackerOutputName).append(" is hurt by the sea of fire!");
                            break;
                        case "jumpkick":
                        case "highjumpkick":
                            toAppendBuilder.append(attackerOutputName).append(" kept going and crashed!");
                            break;
                        case "rockyhelmet":
                            PokemonInfo helmetPokemon = battleFragment.getPokemonInfo(split[3]);
                            if (helmetPokemon != null) {
                                helmetPokemon.setItem("rockyhelmet");
                            }
                            toAppendBuilder.append(attackerOutputName).append(" is hurt by ").append(battleFragment.getPrintable(ofSource)).append("'s ").append(" Rocky Helmet!");
                            break;
                        default:
                            if (ofSource != null) {
                                toAppendBuilder.append(attackerOutputName).append(" is hurt by ").append(battleFragment.getPrintable(ofSource)).append("'s ").append(battleFragment.getPrintable(fromEffect)).append("!");
                            } else if (fromEffectId.contains(":")) {
                                toAppendBuilder.append(attackerOutputName).append(" is hurt by its").append(battleFragment.getPrintable(fromEffect)).append("!");
                            } else {
                                toAppendBuilder.append(attackerOutputName).append(" lost some HP because of ").append(battleFragment.getPrintable(fromEffect)).append("!");
                            }
                            break;
                    }
                } else {
                    toAppendBuilder.append(attackerOutputName).append(" lost ");
                    toAppendBuilder.append(-lostHP).append("% of its health!");
                }

                toast = battleFragment.makeMinorToast(new SpannableStringBuilder(toAppendBuilder));

                final TextView damage = new TextView(battleFragment.getActivity());
                damage.setText(lostHP + "%");
                damage.setBackgroundResource(R.drawable.editable_frame_light_red);
                damage.setPadding(2, 2, 2, 2);
                damage.setAlpha(0f);

                toast.addListener(new AnimatorListenerWithNet() {
                    @Override
                    public void onAnimationStartWithNet(Animator animation) {
                        if (battleFragment.getView() == null) {
                            return;
                        }
                        ((TextView) battleFragment.getView().findViewById(battleFragment.getHpId(messageDetails))).setText(Integer.toString(intAmount));

                        ImageView imageView = (ImageView) battleFragment.getView().findViewById(battleFragment.getSpriteId(messageDetails));

                        RelativeLayout relativeLayout = (RelativeLayout) battleFragment.getView().findViewById(battleFragment.getPkmLayoutId(messageDetails));
                        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                        layoutParams.addRule(RelativeLayout.ALIGN_TOP, battleFragment.getSpriteId(messageDetails));
                        layoutParams.addRule(RelativeLayout.ALIGN_LEFT, battleFragment.getSpriteId(messageDetails));
                        layoutParams.setMargins((int) (imageView.getWidth() * 0.5f), (int) (imageView.getHeight() * 0.5f), 0, 0);
                        relativeLayout.addView(damage, layoutParams);
                    }

                    @Override
                    public void onAnimationEndWithNet(Animator animation) {
                        if (battleFragment.getView() == null) {
                            return;
                        }

                        RelativeLayout relativeLayout = (RelativeLayout) battleFragment.getView().findViewById(battleFragment.getPkmLayoutId(messageDetails));
                        relativeLayout.removeView(damage);
                    }
                });

                ObjectAnimator fadeIn = ObjectAnimator.ofFloat(damage, "alpha", 0f, 1f);
                fadeIn.setInterpolator(new DecelerateInterpolator());
                fadeIn.setDuration(BattleFragment.ANIMATION_SHORT / 4);

                ObjectAnimator fadeOut = ObjectAnimator.ofFloat(damage, "alpha", 1f, 0f);
                fadeOut.setInterpolator(new AccelerateInterpolator());
                fadeOut.setStartDelay(BattleFragment.ANIMATION_SHORT / 2);
                fadeOut.setDuration(BattleFragment.ANIMATION_SHORT / 4);

                ProgressBar hpBar = (ProgressBar) battleFragment.getView().findViewById(battleFragment.getHpBarId(messageDetails));
                ObjectAnimator hpCountDownBar = ObjectAnimator.ofInt(hpBar, "progress", intAmount);
                hpCountDownBar.setDuration(BattleFragment.ANIMATION_SHORT);
                hpCountDownBar.setInterpolator(new AccelerateDecelerateInterpolator());

                animatorSet = new AnimatorSet();
                animatorSet.play(toast);
                animatorSet.play(hpCountDownBar).with(toast);
                animatorSet.play(fadeIn).with(toast);
                animatorSet.play(fadeOut).after(fadeIn);

                battleFragment.startAnimation(animatorSet, message);

                logMessage = new SpannableStringBuilder(toAppendBuilder);
                break;

            case "-heal":
                attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0]);
                pokemonInfo = battleFragment.getPokemonInfo(messageDetails);
                oldHP = pokemonInfo.getHp();
                intAmount = processHpFraction(split[1]);
                pokemonInfo.setHp(intAmount);
                lostHP = intAmount - oldHP;

                if (fromEffectId != null) {
                    switch (battleFragment.trimOrigin(fromEffect)) {
                        case "ingrain":
                            toAppendBuilder.append(attackerOutputName).append(" absorbed nutrients with its roots!");
                            break;
                        case "aquaring":
                            attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0], false);
                            toAppendBuilder.append("Aqua Ring restored ").append(attackerOutputName).append("'s HP!");
                            break;
                        case "raindish":
                        case "dryskin":
                        case "icebody":
                            toAppendBuilder.append(attackerOutputName).append("'s ").append(battleFragment.getPrintable(fromEffect)).append(" heals it!");
                            if (pokemonInfo != null) {
                                pokemonInfo.setAbility(battleFragment.getPrintable(fromEffect));
                            }
                            break;
                        case "healingwish":
                            attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0], false);
                            toAppendBuilder.append("The healing wish came true for ").append(attackerOutputName);
                            break;
                        case "lunardance":
                            toAppendBuilder.append(attackerOutputName).append(" became cloaked in mystical moonlight!");
                            break;
                        case "wish":
                            //TODO TRY
                            String wisher;
                            if (messageDetails.contains("[wisher]")) {
                                separator = messageDetails.substring(messageDetails.indexOf("[wisher]")).indexOf("|");
                                if (separator != -1) {
                                    wisher = messageDetails.substring(messageDetails.indexOf("[wisher]") + 8, separator);
                                } else {
                                    wisher = messageDetails.substring(messageDetails.indexOf("[wisher]") + 8);
                                }
                                toAppendBuilder.append(battleFragment.getPrintableOutputPokemonSide(wisher)).append("'s wish came true!");
                            }
                            break;
                        case "drain":
                            if (trimmedOfEffect != null) {
                                toAppendBuilder.append(battleFragment.getPrintableOutputPokemonSide(ofSource)).append(" had its energy drained!");
                                break;
                            }
                            // we should never enter here
                            toAppendBuilder.append(attackerOutputName).append(" drained health!");
                            break;

                        case "leftovers":
                        case "shellbell":
                        case "blacksludge":
                            toAppendBuilder.append(attackerOutputName).append(" restored a little HP using its ").append(battleFragment.getPrintable(fromEffect)).append("!");
                            break;
                        default:
                            toAppendBuilder.append(attackerOutputName).append(" restored HP using its ").append(battleFragment.getPrintable(fromEffect)).append("!");
                            break;
                    }
                } else {
                    toAppendBuilder.append(attackerOutputName);
                    toAppendBuilder.append(" healed ").append(lostHP).append("% of it's health!");
                }

                toast = battleFragment.makeMinorToast(new SpannableStringBuilder(toAppendBuilder));

                final TextView heal = new TextView(battleFragment.getActivity());
                heal.setText(lostHP + "%");
                heal.setBackgroundResource(R.drawable.editable_frame_light_green);
                heal.setPadding(2, 2, 2, 2);
                heal.setAlpha(0f);

                toast.addListener(new AnimatorListenerWithNet() {
                    @Override
                    public void onAnimationStartWithNet(Animator animation) {
                        if (battleFragment.getView() == null) {
                            return;
                        }
                        ((TextView) battleFragment.getView().findViewById(battleFragment.getHpId(messageDetails))).setText(Integer.toString(intAmount));

                        ImageView imageView = (ImageView) battleFragment.getView().findViewById(battleFragment.getSpriteId(messageDetails));

                        RelativeLayout relativeLayout = (RelativeLayout) battleFragment.getView().findViewById(battleFragment.getPkmLayoutId(messageDetails));
                        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                        layoutParams.addRule(RelativeLayout.ALIGN_TOP, battleFragment.getSpriteId(messageDetails));
                        layoutParams.addRule(RelativeLayout.ALIGN_LEFT, battleFragment.getSpriteId(messageDetails));
                        layoutParams.setMargins((int) (imageView.getWidth() * 0.5f), (int) (imageView.getHeight() * 0.5f), 0, 0);
                        relativeLayout.addView(heal, layoutParams);
                    }

                    @Override
                    public void onAnimationEndWithNet(Animator animation) {
                        if (battleFragment.getView() == null) {
                            return;
                        }

                        RelativeLayout relativeLayout = (RelativeLayout) battleFragment.getView().findViewById(battleFragment.getPkmLayoutId(messageDetails));
                        relativeLayout.removeView(heal);
                    }
                });

                fadeIn = ObjectAnimator.ofFloat(heal, "alpha", 0f, 1f);
                fadeIn.setInterpolator(new DecelerateInterpolator());
                fadeIn.setDuration(BattleFragment.ANIMATION_SHORT / 4);

                fadeOut = ObjectAnimator.ofFloat(heal, "alpha", 1f, 0f);
                fadeOut.setInterpolator(new AccelerateInterpolator());
                fadeOut.setStartDelay(BattleFragment.ANIMATION_SHORT / 2);
                fadeOut.setDuration(BattleFragment.ANIMATION_SHORT / 4);

                hpBar = (ProgressBar) battleFragment.getView().findViewById(battleFragment.getHpBarId(messageDetails));
                hpCountDownBar = ObjectAnimator.ofInt(hpBar, "progress", intAmount);
                hpCountDownBar.setDuration(BattleFragment.ANIMATION_SHORT);
                hpCountDownBar.setInterpolator(new AccelerateDecelerateInterpolator());

                animatorSet = new AnimatorSet();
                animatorSet.play(toast);
                animatorSet.play(hpCountDownBar).with(toast);
                animatorSet.play(fadeIn).with(toast);
                animatorSet.play(fadeOut).after(fadeIn);

                battleFragment.startAnimation(animatorSet, message);

                logMessage = new SpannableStringBuilder(toAppendBuilder);
                break;
            case "-sethp":
                switch (battleFragment.trimOrigin(fromEffect)) {
                    case "painsplit":
                        toAppendBuilder.append("The battlers shared their pain!");
                        toast = battleFragment.makeMinorToast(new SpannableString(toAppendBuilder));

                        PokemonInfo pkmA = battleFragment.getPokemonInfo(split[0]);
                        final int pkmAHp = processHpFraction(split[1]);
                        pkmA.setHp(pkmAHp);
                        PokemonInfo pkmB = battleFragment.getPokemonInfo(split[2]);
                        final int pkmBHp = processHpFraction(split[3]);
                        pkmB.setHp(pkmBHp);

                        toast.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                if (battleFragment.getView() == null) {
                                    return;
                                }

                                ((TextView) battleFragment.getView().findViewById(battleFragment.getHpId(split[0])))
                                        .setText(Integer.toString(pkmAHp));
                                ((TextView) battleFragment.getView().findViewById(battleFragment.getHpId(split[2])))
                                        .setText(Integer.toString(pkmBHp));
                            }
                        });

                        ProgressBar pkmAHpBar = (ProgressBar) battleFragment.getView().findViewById(battleFragment.getHpBarId(split[0]));
                        ObjectAnimator pkmACountDown = ObjectAnimator.ofInt(pkmAHpBar, "progress", pkmAHp);
                        pkmACountDown.setDuration(BattleFragment.ANIMATION_SHORT);
                        pkmACountDown.setInterpolator(new AccelerateDecelerateInterpolator());

                        ProgressBar pkmBHpBar = (ProgressBar) battleFragment.getView().findViewById(battleFragment.getHpBarId(split[2]));
                        ObjectAnimator pkmBCountDown = ObjectAnimator.ofInt(pkmBHpBar, "progress", pkmBHp);
                        pkmBCountDown.setDuration(BattleFragment.ANIMATION_SHORT);
                        pkmBCountDown.setInterpolator(new AccelerateDecelerateInterpolator());

                        animatorSet = new AnimatorSet();
                        animatorSet.play(toast);
                        animatorSet.play(pkmACountDown).with(toast);
                        animatorSet.play(pkmBCountDown).with(toast);

                        battleFragment.startAnimation(animatorSet, message);
                        break;
                }
                logMessage = new SpannableStringBuilder(toAppendBuilder);
                break;

            case "-boost":
                attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0]);
                stat = split[1];
                final String increasedStat;
                increasedStat = stat;
                statAmount = "";
                switch (stat) {
                    case "atk":
                        stat = "Attack";
                        break;
                    case "def":
                        stat = "Defense";
                        break;
                    case "spa":
                        stat = "Special Attack";
                        break;
                    case "spd":
                        stat = "Special Defense";
                        break;
                    case "spe":
                        stat = "Speed";
                        break;
                    default:
                        break;
                }
                String amount = split[2];
                intAmount = Integer.parseInt(amount);
                if (intAmount == 2) {
                    statAmount = " sharply";
                } else if (intAmount > 2) {
                    statAmount = " drastically";
                }

                if (fromEffect != null) {
                    if (fromEffect.contains("item:")) {
                        attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0], false);
                        toAppendBuilder.append("The ").append(battleFragment.getPrintable(fromEffect)).append(statAmount).append(" raised ").append(attackerOutputName).append("'s ").append(stat).append("!");
                    } else {
                        toAppendBuilder.append(attackerOutputName).append("'s ").append(battleFragment.getPrintable(fromEffect)).append(statAmount).append(" raised its ").append(stat).append("!");
                    }
                } else {
                    toAppendBuilder.append(attackerOutputName).append("'s ").append(stat).append(statAmount).append(" rose!");
                }

                toast = battleFragment.makeMinorToast(new SpannableStringBuilder(toAppendBuilder));

                toast.addListener(new AnimatorListenerWithNet() {
                    @Override
                    public void onAnimationStartWithNet(Animator animation) {
                        battleFragment.processBoost(messageDetails, increasedStat, intAmount);
                    }
                });
                battleFragment.startAnimation(toast, message);
                logMessage = new SpannableStringBuilder(toAppendBuilder);
                break;

            case "-unboost":
                attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0]);
                stat = split[1];
                increasedStat = stat;
                statAmount = "";

                switch (stat) {
                    case "atk":
                        stat = "Attack";
                        break;
                    case "def":
                        stat = "Defense";
                        break;
                    case "spa":
                        stat = "Special Attack";
                        break;
                    case "spd":
                        stat = "Special Defense";
                        break;
                    case "spe":
                        stat = "Speed";
                        break;
                    default:
                        break;
                }
                amount = split[2];
                intAmount = -1 * Integer.parseInt(amount);
                if (intAmount == -2) {
                    statAmount = " harshly";
                } else if (intAmount <= -3) {
                    statAmount = " severely";
                }

                if (fromEffect != null) {
                    if (fromEffect.contains("item:")) {
                        attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0], false);
                        toAppendBuilder.append("The ").append(battleFragment.getPrintable(fromEffect)).append(statAmount).append(" lowered ").append(attackerOutputName).append("'s ").append(stat).append("!");
                    } else {
                        toAppendBuilder.append(attackerOutputName).append("'s ").append(battleFragment.getPrintable(fromEffect)).append(statAmount).append(" lowered its ").append(stat).append("!");
                    }
                } else {
                    toAppendBuilder.append(attackerOutputName).append("'s ").append(stat).append(statAmount).append(" fell!");
                }

                toast = battleFragment.makeMinorToast(new SpannableStringBuilder(toAppendBuilder));
                toast.addListener(new AnimatorListenerWithNet() {
                    @Override
                    public void onAnimationStartWithNet(Animator animation) {
                        battleFragment.processBoost(messageDetails, increasedStat, intAmount);
                    }
                });
                battleFragment.startAnimation(toast, message);
                logMessage = new SpannableStringBuilder(toAppendBuilder);
                break;

            case "-setboost":
                attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0]);
                if (fromEffect != null) {
                    switch (battleFragment.trimOrigin(fromEffect)) {
                        case "bellydrum":
                            toAppendBuilder.append(attackerOutputName).append(" cut its own HP and maximized its Attack!");
                            toast = battleFragment.makeMinorToast(new SpannableString(toAppendBuilder));
                            toast.addListener(new AnimatorListenerWithNet() {
                                @Override
                                public void onAnimationStartWithNet(Animator animation) {
                                    battleFragment.processBoost(split[0], "atk", 6);
                                }
                            });
                            battleFragment.startAnimation(toast, message);
                            break;

                        case "angerpoint":
                            toAppendBuilder.append(attackerOutputName).append(" maxed its Attack!");
                            toast = battleFragment.makeMinorToast(new SpannableString(toAppendBuilder));
                            toast.addListener(new AnimatorListenerWithNet() {
                                @Override
                                public void onAnimationStartWithNet(Animator animation) {
                                    battleFragment.processBoost(split[0], "atk", 6);
                                    battleFragment.getPokemonInfo(split[0]).setAbility("angerpoint");
                                }
                            });
                            battleFragment.startAnimation(toast, message);
                            break;
                    }
                }
                logMessage = new SpannableStringBuilder(toAppendBuilder);
                break;

            case "-swapboost":
                attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0]);
                if (fromEffect != null) {
                    switch (battleFragment.trimOrigin(fromEffect)) {
                        case "guardswap":
                            toAppendBuilder.append(attackerOutputName).append(" switched all changes to its Defense and Sp. Def with the target!");
                            toast = battleFragment.makeMinorToast(new SpannableStringBuilder(toAppendBuilder));
                            toast.addListener(new AnimatorListenerWithNet() {
                                @Override
                                public void onAnimationStartWithNet(Animator animation) {
                                    battleFragment.swapBoost(split[0], split[1], "def", "spd");
                                }
                            });
                            battleFragment.startAnimation(toast, message);
                            break;

                        case "heartswap":
                            toAppendBuilder.append(attackerOutputName).append(" switched stat changes with the target!");
                            toast = battleFragment.makeMinorToast(new SpannableStringBuilder(toAppendBuilder));
                            toast.addListener(new AnimatorListenerWithNet() {
                                @Override
                                public void onAnimationStartWithNet(Animator animation) {
                                    battleFragment.swapBoost(split[0], split[1], BattleFragment.STATS);
                                }
                            });
                            battleFragment.startAnimation(toast, message);
                            break;

                        case "powerswap":
                            toAppendBuilder.append(attackerOutputName).append(" switched all changes to its Attack and Sp. Atk with the target!");
                            toast = battleFragment.makeMinorToast(new SpannableStringBuilder(toAppendBuilder));
                            toast.addListener(new AnimatorListenerWithNet() {
                                @Override
                                public void onAnimationStartWithNet(Animator animation) {
                                    battleFragment.swapBoost(split[0], split[1], "atk", "spa");
                                }
                            });
                            battleFragment.startAnimation(toast, message);
                            break;
                    }
                    logMessage = new SpannableStringBuilder(toAppendBuilder);
                }
                break;

            case "-restoreboost":
                attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0]);
                toAppendBuilder.append(attackerOutputName).append("'s negative stat changes were removed!");
                toast = battleFragment.makeMinorToast(new SpannableStringBuilder(toAppendBuilder));
                toast.addListener(new AnimatorListenerWithNet() {
                    @Override
                    public void onAnimationStartWithNet(Animator animation) {
                        if (battleFragment.getView() == null) {
                            return;
                        }
                        battleFragment.restoreBoost(split[0]);
                    }
                });
                battleFragment.startAnimation(toast, message);
                logMessage = new SpannableStringBuilder(toAppendBuilder);
                break;

            case "-clearnegativeboost":
                attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0]);
                toAppendBuilder.append(attackerOutputName).append("'s negative stat changes were removed!");
                toast = battleFragment.makeMinorToast(new SpannableStringBuilder(toAppendBuilder));
                toast.addListener(new AnimatorListenerWithNet() {
                    @Override
                    public void onAnimationStartWithNet(Animator animation) {
                        if (battleFragment.getView() == null) {
                            return;
                        }
                        battleFragment.clearnegativeBoost(split[0]);
                    }
                });
                battleFragment.startAnimation(toast, message);
                logMessage = new SpannableStringBuilder(toAppendBuilder);
                break;

            case "-clearpositiveboost":
                attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0]);
                toAppendBuilder.append(attackerOutputName).append("'s positive stat changes were removed!");
                toast = battleFragment.makeMinorToast(new SpannableStringBuilder(toAppendBuilder));
                toast.addListener(new AnimatorListenerWithNet() {
                    @Override
                    public void onAnimationStartWithNet(Animator animation) {
                        if (battleFragment.getView() == null) {
                            return;
                        }
                        battleFragment.clearpositiveBoost(split[0]);
                    }
                });
                battleFragment.startAnimation(toast, message);
                logMessage = new SpannableStringBuilder(toAppendBuilder);
                break;

            case "-copyboost":
                attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0]);
                defenderOutputName = battleFragment.getPrintableOutputPokemonSide(split[1], false);
                toAppendBuilder.append(attackerOutputName).append(" copied ").append(defenderOutputName).append("'s stat changes!");
                toast = battleFragment.makeMinorToast(new SpannableStringBuilder(toAppendBuilder));
                toast.addListener(new AnimatorListenerWithNet() {
                    @Override
                    public void onAnimationStartWithNet(Animator animation) {
                        if (battleFragment.getView() == null) {
                            return;
                        }
                        LinearLayout linearLayout = (LinearLayout) battleFragment.getView().findViewById(battleFragment.getTempStatusId(split[0]));
                        for (String stat : BattleFragment.STATS) {
                            TextView v = (TextView) linearLayout.findViewWithTag(stat);
                            linearLayout.removeView(v);
                        }
                        battleFragment.copyBoost(split[1], split[0]);
                    }
                });
                battleFragment.startAnimation(toast, message);
                logMessage = new SpannableStringBuilder(toAppendBuilder);
                break;

            case "-clearboost":
                attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0]);
                toAppendBuilder.append(attackerOutputName).append("'s stat changes were removed!");
                toast = battleFragment.makeMinorToast(new SpannableStringBuilder(toAppendBuilder));
                toast.addListener(new AnimatorListenerWithNet() {
                    @Override
                    public void onAnimationStartWithNet(Animator animation) {
                        if (battleFragment.getView() == null) {
                            return;
                        }
                        LinearLayout linearLayout = (LinearLayout) battleFragment.getView().findViewById(battleFragment.getTempStatusId(split[0]));
                        for (String stat : BattleFragment.STATS) {
                            TextView v = (TextView) linearLayout.findViewWithTag(stat);
                            linearLayout.removeView(v);
                        }
                    }
                });
                battleFragment.startAnimation(toast, message);
                logMessage = new SpannableStringBuilder(toAppendBuilder);
                break;

            case "-invertboost":
                attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0]);
                toAppendBuilder.append(attackerOutputName).append("'s stat changes were inverted!");
                toast = battleFragment.makeMinorToast(new SpannableStringBuilder(toAppendBuilder));
                toast.addListener(new AnimatorListenerWithNet() {
                    @Override
                    public void onAnimationStartWithNet(Animator animation) {
                        battleFragment.invertBoost(split[0], BattleFragment.STATS);
                    }
                });
                battleFragment.startAnimation(toast, message);
                logMessage = new SpannableStringBuilder(toAppendBuilder);
                break;

            case "-clearallboost":
                toAppendBuilder.append("All stat changes were eliminated!");
                toast = battleFragment.makeMinorToast(new SpannableStringBuilder(toAppendBuilder));
                toast.addListener(new AnimatorListenerWithNet() {
                    @Override
                    public void onAnimationStartWithNet(Animator animation) {
                        if (battleFragment.getView() == null) {
                            return;
                        }
                        String[] layouts = {"p1a", "p1b", "p1c", "p2a", "p2b", "p2c"};
                        for (String layout : layouts) {
                            LinearLayout linearLayout = (LinearLayout) battleFragment.getView().findViewById(battleFragment.getTempStatusId(layout));
                            for (String stat : BattleFragment.STATS) {
                                TextView v = (TextView) linearLayout.findViewWithTag(stat);
                                linearLayout.removeView(v);
                            }
                        }
                    }
                });
                battleFragment.startAnimation(toast, message);
                logMessage = new SpannableStringBuilder(toAppendBuilder);
                break;

            case "-crit":
                toAppendSpannable = new SpannableString("It's a critical hit!");
                toast = battleFragment.makeMinorToast(toAppendSpannable);
                animatorSet = battleFragment.createFlyingMessage(split[0], toast, new SpannableString("Critical!"));
                battleFragment.startAnimation(animatorSet, message);
                logMessage = new SpannableStringBuilder(toAppendSpannable);
                break;

            case "-supereffective":
                toAppendSpannable = new SpannableString("It's super effective!");
                toast = battleFragment.makeMinorToast(toAppendSpannable);
                animatorSet = battleFragment.createFlyingMessage(split[0], toast, new SpannableString("Super Effective!"));
                battleFragment.startAnimation(animatorSet, message);
                logMessage = new SpannableStringBuilder(toAppendSpannable);
                break;

            case "-resisted":
                toAppendSpannable = new SpannableString("It's not very effective...");
                toast = battleFragment.makeMinorToast(toAppendSpannable);
                animatorSet = battleFragment.createFlyingMessage(split[0], toast, new SpannableString("Resisted!"));
                battleFragment.startAnimation(animatorSet, message);
                logMessage = new SpannableStringBuilder(toAppendSpannable);
                break;

            case "-immune":
                attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0], false);
                toAppendBuilder.append("It doesn't affect ");
                toAppendBuilder.append(attackerOutputName);
                toAppendBuilder.append(".");
                toAppendSpannable = new SpannableString(toAppendBuilder);
                toast = battleFragment.makeMinorToast(toAppendSpannable);
                animatorSet = battleFragment.createFlyingMessage(split[0], toast, new SpannableString("Immuned!"));
                battleFragment.startAnimation(animatorSet, message);
                logMessage = new SpannableStringBuilder(toAppendBuilder);
                break;

            case "-miss":
                if (split.length > 1) {
                    // there was a target
                    defenderOutputName = battleFragment.getPrintableOutputPokemonSide(split[1]);
                    toAppendBuilder.append(defenderOutputName).append(" avoided the attack!");
                } else {
                    attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0]);
                    toAppendBuilder.append(attackerOutputName).append("'s attack missed!");
                }
                toAppendSpannable = new SpannableStringBuilder(toAppendBuilder);
                toast = battleFragment.makeMinorToast(toAppendSpannable);
                animatorSet = battleFragment.createFlyingMessage(split[0], toast, new SpannableString("Missed!"));
                battleFragment.startAnimation(animatorSet, message);
                logMessage = new SpannableStringBuilder(toAppendBuilder);
                break;

            case "-fail":
                attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0]);
                if (split.length > 1) {
                    remaining = split[1];

                    switch (remaining) {
                        case "brn":
                            toAppendBuilder.append(attackerOutputName).append(" is already burned.");
                            break;
                        case "tox":
                        case "psn":
                            toAppendBuilder.append(attackerOutputName).append(" is already poisoned.");
                            break;
                        case "slp":
                            if (fromEffect != null && battleFragment.getPrintable(fromEffectId).equals("uproar")) {
                                attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0], false);
                                toAppendBuilder.append("But the uproar kept ").append(attackerOutputName).append(" awake!");
                            } else {
                                toAppendBuilder.append(attackerOutputName).append(" is already asleep.");
                            }
                            break;
                        case "par":
                            toAppendBuilder.append(attackerOutputName).append(" is already paralyzed.");
                            break;
                        case "frz":
                            toAppendBuilder.append(attackerOutputName).append(" is already frozen.");
                            break;
                        case "hyperspacefury":
                            toAppendBuilder.append(attackerOutputName).append(" can't use the move!");
                            break;
                        case "magikarpsrevenge":
                            toAppendBuilder.append("But ").append(attackerOutputName).append(" can't use the move!");
                            break;
                        case "substitute":
                            if (messageDetails.contains("[weak]")) {
                                toAppendBuilder.append(attackerOutputName).append("It was too weak to make a substitute!");
                            } else {
                                toAppendBuilder.append(attackerOutputName).append(" already has a substitute!");
                            }
                            break;
                        case "skydrop":
                            if (messageDetails.contains("[heavy]")) {
                                toAppendBuilder.append(attackerOutputName).append(" is too heavy to be lifted!");
                            } else {
                                toAppendBuilder.append("But it failed!");
                            }
                            break;
                        case "sunnyday":
                        case "raindance":
                        case "sandstorm":
                        case "hail":
                            switch (battleFragment.trimOrigin(fromEffect)) {
                                case "desolateland":
                                    toAppendBuilder.append("The extremely harsh sunlight was not lessened at all!");
                                    break;
                                case "primordialsea":
                                    toAppendBuilder.append("There's no relief from this heavy rain!");
                                    break;
                                case "deltastream":
                                    toAppendBuilder.append("The mysterious air current blows on regardless!");
                                    break;
                                default:
                                    toAppendBuilder.append("But it failed!");
                            }
                            break;
                        case "unboost":
                            toAppendBuilder.append(attackerOutputName).append("'s stats were not lowered!");
                            break;

                        default:
                            if (fromEffect != null) {
                                switch (battleFragment.trimOrigin(fromEffect)) {
                                    case "desolateland":
                                        toAppendBuilder.append("The Water-type attack evaporated in the harsh sunlight!");
                                        break;
                                    case "primordialsea":
                                        toAppendBuilder.append("The Fire-type attack fizzled out in the heavy rain!");
                                        break;
                                    default:
                                        toAppendBuilder.append("But it failed!");
                                }
                            } else {
                                toAppendBuilder.append("But it failed!");
                            }
                            break;
                    }
                } else {
                    toAppendBuilder.append("But it failed!");
                }
                toast = battleFragment.makeMinorToast(new SpannableString(toAppendBuilder));
                animatorSet = battleFragment.createFlyingMessage(split[0], toast, new SpannableString("But it failed!"));
                battleFragment.startAnimation(animatorSet, message);

                logMessage = new SpannableString(toAppendBuilder);
                break;

            case "-notarget":
                logMessage = new SpannableString("But there was no target...");
                toast = battleFragment.makeMinorToast(logMessage);
                battleFragment.startAnimation(toast, message);
                break;

            case "-ohko":
                logMessage = new SpannableString("It's a one-hit KO!");
                toast = battleFragment.makeMinorToast(logMessage);
                battleFragment.startAnimation(toast, message);
                break;

            case "-hitcount":
                try {
                    String hitCountS = split[split.length - 1];
                    int hitCount = Integer.parseInt(hitCountS);
                    toAppendBuilder.append("Hit ").append(hitCount).append(" time");
                    if (hitCount > 1) {
                        toAppendBuilder.append("s");
                    }
                    toAppendBuilder.append("!");
                    logMessage = new SpannableStringBuilder(toAppendBuilder);
                } catch (NumberFormatException e) {
                    logMessage = new SpannableString(command + ":" + messageDetails);
                }
                toast = battleFragment.makeMinorToast(logMessage);
                battleFragment.startAnimation(toast, message);
                break;

            case "-nothing":
                logMessage = new SpannableString("But nothing happened! ");
                toast = battleFragment.makeMinorToast(logMessage);
                battleFragment.startAnimation(toast, message);
                break;

            case "-waiting":
                attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0]);
                defenderOutputName = battleFragment.getPrintableOutputPokemonSide(split[1], false);
                toAppendBuilder.append(attackerOutputName).append(" is waiting for ").append(defenderOutputName).append("'s move...");
                logMessage = new SpannableString(toAppendBuilder);
                toast = battleFragment.makeMinorToast(logMessage);
                battleFragment.startAnimation(toast, message);
                break;

            case "-combine":
                logMessage = new SpannableString("The two moves are joined! It's a combined move!");
                toast = battleFragment.makeMinorToast(logMessage);
                battleFragment.startAnimation(toast, message);
                break;

            case "-prepare":
                attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0]);
                logMessage = new SpannableString(attackerOutputName + " is absorbing power!");
                toast = battleFragment.makeMinorToast(logMessage);
                battleFragment.startAnimation(toast, message);
                break;

            case "-status":
                attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0]);
                toAppendBuilder.append(attackerOutputName);
                remaining = split[1];
                switch (remaining) {
                    case "brn":
                        toAppendBuilder.append(" was burned");
                        if (fromEffect != null) {
                            toAppendBuilder.append(" by the ").append(battleFragment.getPrintable(fromEffect));
                        }
                        toAppendBuilder.append("!");
                        break;

                    case "tox":
                        toAppendBuilder.append(" was badly poisoned");
                        if (fromEffect != null) {
                            toAppendBuilder.append(" by the ").append(battleFragment.getPrintable(fromEffect));
                        }
                        toAppendBuilder.append("!");
                        break;

                    case "psn":
                        toAppendBuilder.append(" was poisoned!");
                        break;

                    case "slp":
                        if (fromEffect != null && fromEffectId.equals("moverest")) {
                            toAppendBuilder.append(" slept and became healthy!");
                        } else {
                            toAppendBuilder.append(" fell asleep!");
                        }
                        break;

                    case "par":
                        toAppendBuilder.append(" is paralyzed! It may be unable to move!");
                        break;

                    case "frz":
                        toAppendBuilder.append(" was frozen solid!");
                        break;
                }
                logMessage = new SpannableStringBuilder(toAppendBuilder);
                toast = battleFragment.makeMinorToast(logMessage);
                final String status;
                status = remaining;
                final boolean rest = (fromEffectId != null && fromEffectId.equals("moverest"));
                toast.addListener(new AnimatorListenerWithNet() {
                    @Override
                    public void onAnimationStartWithNet(Animator animation) {
                        battleFragment.setAddonStatus(split[0], status);
                        if (rest) {
                            for (String stt : BattleFragment.STTUS) {
                                if (!stt.equals("slp")) {
                                    battleFragment.removeAddonStatus(split[0], stt);
                                }
                            }
                        }
                    }
                });
                battleFragment.startAnimation(toast, message);
                break;

            case "-curestatus":
                attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0]);
                flag = false;
                if (fromEffectId != null) {
                    fromEffectId = battleFragment.getPrintable(fromEffectId);
                    switch (battleFragment.trimOrigin(fromEffect)) {
                        case "psychoshift":
                            defenderOutputName = battleFragment.getPrintableOutputPokemonSide(ofSource, false);
                            toAppendBuilder.append(attackerOutputName).append(" moved its status onto ").append(defenderOutputName);
                            flag = true;
                            break;
                    }
                    if (fromEffectId.contains("ability:")) {
                        toAppendBuilder.append(attackerOutputName).append("'s ").append(battleFragment.getPrintable(fromEffect)).append(" heals its status!");
                        flag = true;
                    }
                }

                if (!flag) {
                    //split1 is cured status
                    switch (split[1]) {
                        case "brn":
                            if (fromEffectId != null && fromEffectId.contains("item:")) {
                                toAppendBuilder.append(attackerOutputName).append("'s ").append(battleFragment.getPrintable(fromEffect)).append(" healed its burn!");
                                break;
                            }
                            if (split[0].startsWith("p2")) {
                                toAppendBuilder.append(attackerOutputName).append("'s burn was healed.");
                            } else {
                                toAppendBuilder.append(attackerOutputName).append(" healed its burn!.");
                            }
                            break;

                        case "tox":
                        case "psn":
                            if (fromEffectId != null && fromEffectId.contains("item:")) {
                                toAppendBuilder.append(attackerOutputName).append("'s ").append(battleFragment.getPrintable(fromEffect)).append(" cured its poison!");
                                break;
                            }
                            toAppendBuilder.append(attackerOutputName).append(" was cured of its poisoning.");
                            break;

                        case "slp":
                            if (fromEffectId != null && fromEffectId.contains("item:")) {
                                toAppendBuilder.append(attackerOutputName).append("'s ").append(battleFragment.getPrintable(fromEffect)).append(" woke it up!");
                                break;
                            }
                            toAppendBuilder.append(attackerOutputName).append(" woke up!");
                            break;

                        case "par":
                            if (fromEffectId != null && fromEffectId.contains("item:")) {
                                toAppendBuilder.append(attackerOutputName).append("'s ").append(battleFragment.getPrintable(fromEffect)).append(" cured its paralysis!");
                                break;
                            }
                            toAppendBuilder.append(attackerOutputName).append(" was cured of paralysis.");

                            break;

                        case "frz":
                            if (fromEffectId != null && fromEffectId.contains("item:")) {
                                toAppendBuilder.append(attackerOutputName).append("'s ").append(battleFragment.getPrintable(fromEffect)).append(" defrosted it!");
                                break;
                            }
                            toAppendBuilder.append(attackerOutputName).append(" thawed out!");
                            break;

                        default:
                            //confusion
                            toAppendBuilder.append(attackerOutputName).append("'s status cleared!");
                            break;
                    }
                }
                logMessage = new SpannableStringBuilder(toAppendBuilder);
                toast = battleFragment.makeMinorToast(logMessage);
                toast.addListener(new AnimatorListenerWithNet() {
                    @Override
                    public void onAnimationStartWithNet(Animator animation) {
                        battleFragment.removeAddonStatus(split[0], split[1]);
                    }
                });
                battleFragment.startAnimation(toast, message);
                break;

            case "-cureteam":
                if (fromEffectId != null) {
                    switch (battleFragment.trimOrigin(fromEffect)) {
                        case "aromatherapy":
                            toAppendBuilder.append("A soothing aroma wafted through the area!");
                            break;

                        case "healbell":
                            toAppendBuilder.append("A bell chimed!");
                            break;
                    }
                } else {
                    attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0]);
                    toAppendBuilder.append(attackerOutputName);
                    toAppendBuilder.append(" 's team was cured");
                }
                logMessage = new SpannableStringBuilder(toAppendBuilder);
                toast = battleFragment.makeMinorToast(logMessage);
                toast.addListener(new AnimatorListenerWithNet() {
                    @Override
                    public void onAnimationStartWithNet(Animator animation) {
                        String[] teammate;
                        if (split[0].startsWith("p1")) {
                            teammate = BattleFragment.TEAMMATES[0];
                        } else {
                            teammate = BattleFragment.TEAMMATES[1];
                        }
                        for (String mate : teammate) {
                            for (String stt : BattleFragment.STTUS) {
                                battleFragment.removeAddonStatus(mate, stt);
                            }
                        }
                    }
                });
                battleFragment.startAnimation(toast, message);
                break;

            case "-item":
                attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0]);
                final String item;
                item = battleFragment.getPrintable(split[1]);
                if (fromEffect != null) {
                    // not to deal with item: or ability: or move:
                    switch (battleFragment.trimOrigin(fromEffect)) {
                        case "recycle":
                        case "pickup":
                            toAppendBuilder.append(attackerOutputName).append(" found one ").append(item).append("!");
                            break;

                        case "frisk":
                            toAppendBuilder.append(battleFragment.getPrintableOutputPokemonSide(ofSource))
                                    .append(" frisked its target and found one ").append(item).append("!");
                            break;

                        case "thief":
                        case "covet":
                            defenderOutputName = battleFragment.getPrintableOutputPokemonSide(ofSource, false);
                            toAppendBuilder.append(attackerOutputName).append("  stole  ").append(defenderOutputName).append("'s ").append(item).append("!");
                            break;

                        case "harvest":
                            toAppendBuilder.append(attackerOutputName).append(" harvested one ").append(item).append("!");
                            break;

                        case "bestow":
                            defenderOutputName = battleFragment.getPrintableOutputPokemonSide(ofSource, false);
                            toAppendBuilder.append(attackerOutputName).append(" received ").append(item).append(" from ").append(defenderOutputName).append("!");
                            break;

                        default:
                            toAppendBuilder.append(attackerOutputName).append(" obtained one ").append(item).append(".");
                            break;
                    }
                    logMessage = new SpannableString(toAppendBuilder);
                    toast = battleFragment.makeMinorToast(logMessage);
                    animatorSet = battleFragment.createFlyingMessage(split[0], toast, new SpannableString(item));
                    battleFragment.startAnimation(animatorSet, message);
                } else {
                    switch (item) {
                        case "Air Balloon":
                            toAppendBuilder.append(attackerOutputName).append(" floats in the air with its Air Balloon!");
                            break;

                        default:
                            toAppendBuilder.append(attackerOutputName).append("has ").append(item).append("!");
                            break;
                    }
                    logMessage = new SpannableString(toAppendBuilder);
                    toast = battleFragment.makeMinorToast(logMessage);
                    toast.addListener(new AnimatorListenerWithNet() {
                        @Override
                        public void onAnimationStartWithNet(Animator animation) {
                            battleFragment.setAddonStatus(split[0], item);
                        }
                    });
                    battleFragment.startAnimation(toast, message);
                }
                pokemonInfo = battleFragment.getPokemonInfo(split[0]);
                pokemonInfo.setItem(item);
                break;

            case "-enditem":
                eat = messageDetails.contains("[eat]");
                weaken = messageDetails.contains("[weaken]");
                attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0]);
                item = split[1].trim();

                if (eat) {
                    toAppendBuilder.append(attackerOutputName).append(" ate its ").append(item).append("!");
                } else if (weaken) {
                    toAppendBuilder.append(attackerOutputName).append(" weakened the damage to ").append(item).append("!");
                } else if (fromEffect != null) {
                    switch (battleFragment.trimOrigin(fromEffect)) {
                        case "fling":
                            toAppendBuilder.append(attackerOutputName).append(" flung its ").append(item).append("!");
                            break;

                        case "knockoff":
                            defenderOutputName = battleFragment.getPrintableOutputPokemonSide(ofSource);
                            attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0], false);

                            toAppendBuilder.append(defenderOutputName).append(" knocked off ").append(attackerOutputName).append("'s ").append(item).append("!");
                            break;

                        case "stealeat":
                            defenderOutputName = battleFragment.getPrintableOutputPokemonSide(ofSource);
                            toAppendBuilder.append(defenderOutputName).append(" stole and ate its target's ").append(item).append("!");
                            break;

                        case "gem":
                            separator = messageDetails.indexOf("[move]");
                            move = "";
                            if (separator != -1) {
                                move = messageDetails.substring(separator + 6);
                                if (move.contains("|")) {
                                    move = move.substring(0, move.indexOf("|"));
                                }
                            }
                            toAppendBuilder.append("The ").append(item).append(" strengthened ").append(move).append("'s power!");
                            break;

                        case "incinerate":
                            toAppendBuilder.append(attackerOutputName).append("'s ").append(item).append(" was burnt up!");
                            break;

                        default:
                            toAppendBuilder.append(attackerOutputName).append(" lost its ").append(item).append("!");
                            break;
                    }
                } else {
                    String itemId = MyApplication.toId(item);
                    switch (itemId) {
                        case "airballoon":
                            toAppendBuilder.append(attackerOutputName).append("'s Air Balloon popped!");
                            break;

                        case "focussash":
                            toAppendBuilder.append(attackerOutputName).append(" hung on using its Focus Sash!");
                            break;

                        case "focusband":
                            toAppendBuilder.append(attackerOutputName).append(" hung on using its Focus Band!");
                            break;

                        case "mentalherb":
                            toAppendBuilder.append(attackerOutputName).append(" used its Mental Herb to come back to its senses!");
                            break;

                        case "whiteherb":
                            toAppendBuilder.append(attackerOutputName).append(" restored its status using its White Herb!");
                            break;

                        case "ejectbutton":
                            toAppendBuilder.append(attackerOutputName).append(" is switched out with the Eject Button!");
                            break;

                        case "redcard":
                            defenderOutputName = battleFragment.getPrintableOutputPokemonSide(ofSource, false);
                            toAppendBuilder.append(attackerOutputName).append(" held up its Red Card against ").append(defenderOutputName).append("!");
                            break;

                        default:
                            toAppendBuilder.append(attackerOutputName).append("'s ").append(item).append(" activated!");
                            break;
                    }
                }

                logMessage = new SpannableString(toAppendBuilder);
                toast = battleFragment.makeMinorToast(logMessage);
                animatorSet = battleFragment.createFlyingMessage(split[0], toast, new SpannableString(item));
                animatorSet.addListener(new AnimatorListenerWithNet() {
                    @Override
                    public void onAnimationStartWithNet(Animator animation) {
                        PokemonInfo pkm = battleFragment.getPokemonInfo(split[0]);
                        pkm.setItem(null);
                        battleFragment.removeAddonStatus(split[0], item);
                    }
                });
                battleFragment.startAnimation(animatorSet, message);
                break;

            case "-ability":
                attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0]);
                ability = split[1];

                if (fromEffect != null) {
                    switch (battleFragment.trimOrigin(fromEffect)) {
                        case "trace":
                            defenderOutputName = battleFragment.getPrintableOutputPokemonSide(ofSource, false);
                            toAppendBuilder.append(attackerOutputName).append(" traced ").append(defenderOutputName).append("'s ").append(battleFragment.getPrintable(ability)).append("!");
                            break;

                        case "roleplay":
                            defenderOutputName = battleFragment.getPrintableOutputPokemonSide(ofSource, false);
                            toAppendBuilder.append(attackerOutputName).append(" copied ").append(defenderOutputName).append("'s ").append(battleFragment.getPrintable(ability)).append("!");
                            break;

                        case "mummy":
                            toAppendBuilder.append(attackerOutputName).append("'s Ability became Mummy!");
                            break;

                        case "desolateland":
                            if (messageDetails.contains("[fail]")) {
                                toAppendBuilder.append("[").append(attackerOutputName).append("'s ").append(ability).append("] The extremely harsh sunlight was not lessened at all!");
                            }
                            break;
                        case "primordialsea":
                            if (messageDetails.contains("[fail]")) {
                                toAppendBuilder.append("[").append(attackerOutputName).append("'s ").append(ability).append("] There's no relief from this heavy rain!");
                            }
                            break;
                        case "deltastream":
                            if (messageDetails.contains("[fail]")) {
                                toAppendBuilder.append("[").append(attackerOutputName).append("'s ").append(ability).append("] The mysterious air current blows on regardless!");
                            }
                            break;

                        default:
                            toAppendBuilder.append(attackerOutputName).append(" acquired ").append(ability).append("!");
                            break;
                    }
                    pokemonInfo = battleFragment.getPokemonInfo(split[0]);
                    if (pokemonInfo != null) {
                        pokemonInfo.setAbility(MyApplication.toId(battleFragment.getPrintable(ability)));
                    }
                    PokemonInfo defInfo = battleFragment.getPokemonInfo(fromEffect);
                    if (defInfo != null) {
                        defInfo.setAbility(MyApplication.toId(battleFragment.getPrintable(ability)));
                    }
                } else {
                    switch (MyApplication.toId(ability)) {
                        case "pressure":
                            toAppendBuilder.append(attackerOutputName).append(" is exerting its pressure!");
                            break;

                        case "moldbreaker":
                            toAppendBuilder.append(attackerOutputName).append(" breaks the mold!");
                            break;

                        case "turboblaze":
                            toAppendBuilder.append(attackerOutputName).append(" is radiating a blazing aura!");
                            break;

                        case "teravolt":
                            toAppendBuilder.append(attackerOutputName).append(" is radiating a bursting aura!");
                            break;

                        case "intimidate":
                            toAppendBuilder.append(attackerOutputName).append("'s Intimidate!");
                            break;

                        case "unnerve":
                            if (split[0].startsWith("p2")) {
                                side = "your team";
                            } else {
                                side = "the opposing team";
                            }
                            toAppendBuilder.append(attackerOutputName).append(" 's Unnerve makes ").append(side).append(" too nervous to eat Berries!");
                            break;

                        case "aurabreak":
                            toAppendBuilder.append(attackerOutputName).append(" reversed all other Pokémon's auras!");
                            break;

                        case "fairyaura":
                            toAppendBuilder.append(attackerOutputName).append(" is radiating a fairy aura!");
                            break;

                        case "darkaura":
                            toAppendBuilder.append(attackerOutputName).append(" is radiating a dark aura!");
                            break;

                        case "airlock":
                        case "cloudnine":
                            toAppendBuilder.append("The effects of weather disappeared.");
                            break;

                        default:
                            toAppendBuilder.append(attackerOutputName).append(" has ").append(battleFragment.getPrintable(ability)).append("!");
                            break;
                    }
                    pokemonInfo = battleFragment.getPokemonInfo(split[0]);
                    if (pokemonInfo != null) {
                        pokemonInfo.setAbility(MyApplication.toId(battleFragment.getPrintable(ability)));
                    }
                }
                logMessage = new SpannableString(toAppendBuilder);
                toast = battleFragment.makeToast(logMessage);
                battleFragment.startAnimation(toast, message);
                break;

            case "-endability":
                attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0]);
                ability = split[1];

                if (fromEffect != null) {
                    switch (battleFragment.trimOrigin(fromEffect)) {
                        case "mummy":
                            attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0], false);
                            toAppendBuilder.append("(").append(attackerOutputName).append("'s Ability was previously ").append(battleFragment.getPrintable(ability)).append(")");
                            pokemonInfo = battleFragment.getPokemonInfo(split[0]);
                            if (pokemonInfo != null) {
                                pokemonInfo.setAbility(MyApplication.toId(battleFragment.getPrintable(ability)));
                            }
                            break;

                        default:
                            toAppendBuilder.append(attackerOutputName).append("\\'s Ability was suppressed!");
                            break;
                    }
                }
                logMessage = new SpannableString(toAppendBuilder);
                toast = battleFragment.makeToast(logMessage);
                battleFragment.startAnimation(toast, message);
                break;

            case "-transform":
                attacker = battleFragment.getPrintableOutputPokemonSide(split[0]);
                defender = battleFragment.getPrintable(split[1]);
                toAppend = attacker + " transformed into " + defender + "!";
                logMessage = new SpannableString(toAppend);
                toast = battleFragment.makeMinorToast(logMessage);
                toast.addListener(new AnimatorListenerWithNet() {
                    @Override
                    public void onAnimationStartWithNet(Animator animation) {
                        if (battleFragment.getView() == null) {
                            return;
                        }
                        ImageView orgn = (ImageView) battleFragment.getView().findViewById(battleFragment.getSpriteId(split[0]));
                        ImageView dest = (ImageView) battleFragment.getView().findViewById(battleFragment.getSpriteId(split[1]));
                        orgn.setImageDrawable(dest.getDrawable());
                        battleFragment.copyBoost(split[1], split[0]);
                    }
                });
                battleFragment.startAnimation(toast, message);
                break;

            case "-formechange":
                final String oldForm = split[0];
                final String oldFormPrintable = battleFragment.getPrintableOutputPokemonSide(split[0]);
                final String newForm = split[1];

                final String newFormPrintable = battleFragment.getPrintableOutputPokemonSide(split[1]);
                switch (MyApplication.toId(newFormPrintable)) {
                    case "darmanitanzen":
                        toAppend = "Zen Mode triggered!";
                        break;
                    case "darmanitan":
                        toAppend = "Zen Mode ended!";
                        break;
                    case "aegislashblade":
                        toAppend = "Changed to Blade Forme!";
                        break;
                    case "aegislash":
                        toAppend = "Changed to Shield Forme!";
                        break;
                    default:
                        toAppend = oldForm + " transformed!";
                        break;
                }

                String position = split[0].substring(0, 3);
                battleFragment.formChange(position, newFormPrintable);
                pokemonInfo = battleFragment.getPokemonInfo(position);

                logMessage = new SpannableString(toAppend);
                toast = battleFragment.makeToast(logMessage, BattleFragment.ANIMATION_SHORT);

                toast.addListener(new AnimatorListenerWithNet() {
                    @Override
                    public void onAnimationStartWithNet(Animator animation) {
                        if (battleFragment.getView() == null) {
                            return;
                        }

                        boolean back = split[0].startsWith("p1");
                        ImageView sprite = (ImageView) battleFragment.getView().findViewById(battleFragment.getSpriteId(oldForm));
                        sprite.setImageResource(Pokemon.getPokemonSprite(battleFragment.getActivity(),
                                MyApplication.toId(newFormPrintable), back, pokemonInfo.isFemale(), pokemonInfo.isShiny()));
                        ImageView icon = (ImageView) battleFragment.getView().findViewById(battleFragment.getIconId(oldForm));
                        icon.setImageResource(Pokemon.getPokemonIcon(battleFragment.getActivity(),
                                MyApplication.toId(newFormPrintable)));
                    }
                });
                battleFragment.startAnimation(toast, message);
                break;

            case "-mega":
                attacker = battleFragment.getPrintableOutputPokemonSide(split[0]);
                toAppendBuilder.append(attacker).append(" has Mega Evolved into Mega ").append(split[1]).append("!");
                logMessage = new SpannableString(toAppendBuilder);
                toast = battleFragment.makeMinorToast(logMessage);
                battleFragment.startAnimation(toast, message);
                break;

            case "-start":
                attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0]);
                animatorSet = new AnimatorSet();
                final String newEffect;
                newEffect = battleFragment.getPrintable(split[1]);
                switch (MyApplication.toId(battleFragment.getPrintable(split[1]))) {
                    case "typechange":
                        if (fromEffect != null) {
                            if (battleFragment.trimOrigin(fromEffect).equals("reflecttype")) {
                                toAppendBuilder.append(attackerOutputName).append("'s type changed to match ").append(battleFragment.getPrintable(ofSource)).append("'s!");
                            } else {
                                toAppendBuilder.append(attackerOutputName).append("'s ").append(battleFragment.getPrintable(fromEffect)).append(" made it the ").append(battleFragment.getPrintable(split[2])).append(" type!");
                            }
                        } else {
                            toAppendBuilder.append(attackerOutputName).append(" transformed into the ").append(battleFragment.getPrintable(split[2])).append(" type!");
                        }
                        break;

                    case "typeadd":
                        attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0], false);
                        toAppendBuilder.append(battleFragment.getPrintable(split[2])).append(" type was added to ").append(attackerOutputName).append(" type!");
                        break;

                    case "powertrick":
                        toAppendBuilder.append(attackerOutputName).append(" switched its Attack and Defense!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                battleFragment.setAddonStatus(split[0], newEffect);
                            }
                        });
                        break;

                    case "foresight":
                    case "miracleeye":
                        toAppendBuilder.append(attackerOutputName).append(" was identified!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                battleFragment.setAddonStatus(split[0], newEffect);
                            }
                        });
                        break;

                    case "telekinesis":
                        toAppendBuilder.append(attackerOutputName).append(" was hurled into the air!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                battleFragment.setAddonStatus(split[0], newEffect);
                            }
                        });
                        break;

                    case "confusion":
                        if (messageDetails.contains("[already]")) {
                            toAppendBuilder.append(attackerOutputName).append(" is already confused!");
                        } else {
                            toAppendBuilder.append(attackerOutputName).append(" became confused!");
                            animatorSet.addListener(new AnimatorListenerWithNet() {
                                @Override
                                public void onAnimationStartWithNet(Animator animation) {
                                    battleFragment.setAddonStatus(split[0], newEffect);
                                }
                            });
                        }
                        break;

                    case "leechseed":
                        toAppendBuilder.append(attackerOutputName).append(" was seeded!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                battleFragment.setAddonStatus(split[0], newEffect);
                            }
                        });
                        break;

                    case "mudsport":
                        toAppendBuilder.append("Electricity's power was weakened!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                battleFragment.setAddonStatus(split[0], newEffect);
                            }
                        });
                        break;

                    case "watersport":
                        toAppendBuilder.append("Fire's power was weakened!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                battleFragment.setAddonStatus(split[0], newEffect);
                            }
                        });
                        break;

                    case "yawn":
                        toAppendBuilder.append(attackerOutputName).append(" grew drowsy!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                battleFragment.setAddonStatus(split[0], newEffect);
                            }
                        });
                        break;

                    case "flashfire":
                        attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0], false);
                        toAppendBuilder.append("The power of ").append(attackerOutputName).append("'s Fire-type moves rose!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                battleFragment.setAddonStatus(split[0], newEffect);
                            }
                        });
                        break;

                    case "taunt":
                        toAppendBuilder.append(attackerOutputName).append(" fell for the taunt!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                battleFragment.setAddonStatus(split[0], newEffect);
                            }
                        });
                        break;

                    case "imprison":
                        toAppendBuilder.append(attackerOutputName).append(" sealed the opponent's move(s)!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                battleFragment.setAddonStatus(split[0], newEffect);
                            }
                        });
                        break;

                    case "disable":
                        toAppendBuilder.append(attackerOutputName).append("'s").append(battleFragment.getPrintable(split[2])).append(" was disabled!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                battleFragment.setAddonStatus(split[0], newEffect);
                            }
                        });
                        break;

                    case "embargo":
                        toAppendBuilder.append(attackerOutputName).append(" can't use items anymore!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                battleFragment.setAddonStatus(split[0], newEffect);
                            }
                        });
                        break;

                    case "ingrain":
                        toAppendBuilder.append(attackerOutputName).append(" planted its roots!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                battleFragment.setAddonStatus(split[0], newEffect);
                            }
                        });
                        break;

                    case "aquaring":
                        toAppendBuilder.append(attackerOutputName).append(" surrounded itself with a veil of water!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                battleFragment.setAddonStatus(split[0], newEffect);
                            }
                        });
                        break;

                    case "stockpile1":
                        toAppendBuilder.append(attackerOutputName).append(" stockpiled 1!");
                        break;

                    case "stockpile2":
                        toAppendBuilder.append(attackerOutputName).append(" stockpiled 2!");
                        break;

                    case "stockpile3":
                        toAppendBuilder.append(attackerOutputName).append(" stockpiled 3!");
                        break;

                    case "perish0":
                        toAppendBuilder.append(attackerOutputName).append("'s perish count fell to 0.");
                        break;

                    case "perish1":
                        toAppendBuilder.append(attackerOutputName).append("'s perish count fell to 1.");
                        break;

                    case "perish2":
                        toAppendBuilder.append(attackerOutputName).append("'s perish count fell to 2.");
                        break;

                    case "perish3":
                        toAppendBuilder.append(attackerOutputName).append("'s perish count fell to 3.");
                        break;

                    case "encore":
                        toAppendBuilder.append(attackerOutputName).append(" received an encore!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                battleFragment.setAddonStatus(split[0], newEffect);
                            }
                        });
                        break;

                    case "bide":
                        toAppendBuilder.append(attackerOutputName).append(" is storing energy!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                battleFragment.setAddonStatus(split[0], newEffect);
                            }
                        });
                        break;

                    case "slowstart":
                        toAppendBuilder.append(attackerOutputName).append(" can't get it going because of its Slow Start!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                battleFragment.setAddonStatus(split[0], newEffect);
                            }
                        });
                        break;

                    case "attract":
                        if (fromEffect != null) {
                            toAppendBuilder.append(attackerOutputName).append(" fell in love from the ").append(battleFragment.getPrintable(fromEffect)).append("!");
                        } else {
                            toAppendBuilder.append(attackerOutputName).append(" fell in love!");
                        }
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                battleFragment.setAddonStatus(split[0], newEffect);
                            }
                        });
                        break;

                    case "autotomize":
                        toAppendBuilder.append(attackerOutputName).append(" became nimble!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                battleFragment.setAddonStatus(split[0], newEffect);
                            }
                        });
                        break;

                    case "focusenergy":
                        toAppendBuilder.append(attackerOutputName).append(" is getting pumped!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                battleFragment.setAddonStatus(split[0], newEffect);
                            }
                        });
                        break;

                    case "curse":
                        attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0], false);
                        toAppendBuilder.append(battleFragment.getPrintableOutputPokemonSide(ofSource)).append(" cut its own HP and laid a curse on ").append(attackerOutputName).append("!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                battleFragment.setAddonStatus(split[0], newEffect);
                            }
                        });
                        break;

                    case "nightmare":
                        toAppendBuilder.append(attackerOutputName).append(" began having a nightmare!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                battleFragment.setAddonStatus(split[0], newEffect);
                            }
                        });
                        break;

                    case "magnetrise":
                        toAppendBuilder.append(attackerOutputName).append(" levitated with electromagnetism!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                battleFragment.setAddonStatus(split[0], newEffect);
                            }
                        });
                        break;

                    case "smackdown":
                        toAppendBuilder.append(attackerOutputName).append(" fell straight down!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                battleFragment.setAddonStatus(split[0], newEffect);
                            }
                        });
                        break;

                    case "substitute":
                        if (messageDetails.contains("[damage]")) {
                            attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0], false);
                            toAppendBuilder.append("The substitute took damage for ").append(attackerOutputName).append("!");
                        } else if (messageDetails.contains("[block]")) {
                            toAppendBuilder.append("But it failed!");
                        } else if (messageDetails.contains("[already]")) {
                            toAppendBuilder.append(attackerOutputName).append(" already has a substitute!");
                        } else {
                            toAppendBuilder.append(attackerOutputName).append(" put in a substitute!");
                            animatorSet.addListener(new AnimatorListenerWithNet() {
                                @Override
                                public void onAnimationStartWithNet(Animator animation) {
                                    if (battleFragment.getView() == null) {
                                        return;
                                    }
                                    ImageView imageView = (ImageView) battleFragment.getView().findViewById(battleFragment.getSpriteId(split[0]));
                                    imageView.setAlpha(0.2f);
                                    ImageView substitute = new ImageView(battleFragment.getActivity());
                                    substitute.setImageResource(battleFragment.getSubstitute(split[0]));
                                    substitute.setTag("Substitute");

                                    RelativeLayout relativeLayout = (RelativeLayout) battleFragment.getView().findViewById(battleFragment.getPkmLayoutId(split[0]));
                                    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                                    layoutParams.addRule(RelativeLayout.ALIGN_TOP, battleFragment.getSpriteId(split[0]));
                                    layoutParams.addRule(RelativeLayout.ALIGN_LEFT, battleFragment.getSpriteId(split[0]));
                                    relativeLayout.addView(substitute, layoutParams);
                                }
                            });
                        }
                        break;

                    case "uproar":
                        if (messageDetails.contains("[upkeep]")) {
                            toAppendBuilder.append(attackerOutputName).append(" is making an uproar!");
                        } else {
                            toAppendBuilder.append(attackerOutputName).append(" caused an uproar!");
                        }
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                battleFragment.setAddonStatus(split[0], newEffect);
                            }
                        });
                        break;

                    case "doomdesire":
                        toAppendBuilder.append(attackerOutputName).append(" chose Doom Desire as its destiny!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                battleFragment.setAddonStatus(split[0], newEffect);
                            }
                        });
                        break;

                    case "futuresight":
                        toAppendBuilder.append(attackerOutputName).append(" foresaw an attack!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                battleFragment.setAddonStatus(split[0], newEffect);
                            }
                        });
                        break;

                    case "mimic":
                        toAppendBuilder.append(attackerOutputName).append(" learned ").append(battleFragment.getPrintable(split[2])).append("!");
                        break;

                    case "followme":
                    case "ragepowder":
                        toAppendBuilder.append(attackerOutputName).append(" became the center of attention!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                battleFragment.setAddonStatus(split[0], newEffect);
                            }
                        });
                        break;

                    case "powder":
                        toAppendBuilder.append(attackerOutputName).append(" is covered in powder!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                battleFragment.setAddonStatus(split[0], newEffect);
                            }
                        });
                        break;

                    default:
                        toAppendBuilder.append(attackerOutputName).append("'s ").append(battleFragment.getPrintable(split[1])).append(" started!");
                        break;
                }
                logMessage = new SpannableString(toAppendBuilder);
                toast = battleFragment.makeMinorToast(logMessage);
                animatorSet.play(toast);
                battleFragment.startAnimation(animatorSet, message);
                break;

            case "-end":
                attacker = split[0];
                attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0]);
                animatorSet = new AnimatorSet();
                newEffect = battleFragment.getPrintable(split[1]);
                animatorSet.addListener(new AnimatorListenerWithNet() {
                    @Override
                    public void onAnimationStartWithNet(Animator animation) {
                        battleFragment.removeAddonStatus(split[0], newEffect);
                    }
                });
                switch (MyApplication.toId(battleFragment.getPrintable(split[1]))) {
                    case "powertrick":
                        toAppendBuilder.append(attackerOutputName).append(" switched its Attack and Defense!");
                        break;

                    case "telekinesis":
                        toAppendBuilder.append(attackerOutputName).append(" was freed from the telekinesis!");
                        break;

                    case "confusion":
                        if (attacker.startsWith("p2")) {
                            toAppendBuilder.append(attackerOutputName).append(" snapped out of confusion!");
                        } else {
                            toAppendBuilder.append(attackerOutputName).append(" snapped out of its confusion.");
                        }
                        break;

                    case "leechseed":
                        if (fromEffect != null && fromEffectId.equals("rapidspin")) {
                            toAppendBuilder.append(attackerOutputName).append(" was freed from Leech Seed!");
                        }
                        break;

                    case "healblock":
                        toAppendBuilder.append(attackerOutputName).append("'s Heal Block wore off!");
                        break;

                    case "taunt":
                        toAppendBuilder.append(attackerOutputName).append("'s taunt wore off!");
                        break;

                    case "disable":
                        toAppendBuilder.append(attackerOutputName).append(" is no longer disabled!");
                        break;

                    case "embargo":
                        toAppendBuilder.append(attackerOutputName).append(" can use items again!");
                        break;

                    case "torment":
                        toAppendBuilder.append(attackerOutputName).append("'s torment wore off!");
                        break;

                    case "encore":
                        toAppendBuilder.append(attackerOutputName).append("'s encore ended!");
                        break;

                    case "bide":
                        toAppendBuilder.append(attackerOutputName).append(" unleashed energy!");
                        break;

                    case "magnetrise":
                        if (attacker.startsWith("p2")) {
                            toAppendBuilder.append("The electromagnetism of ").append(attackerOutputName).append(" wore off!");
                        } else {
                            toAppendBuilder.append(attackerOutputName).append("s electromagnetism wore off!");
                        }
                        break;

                    case "perishsong":
                        break;

                    case "substitute":
                        toAppendBuilder.append(attackerOutputName).append("'s substitute faded!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                if (battleFragment.getView() == null) {
                                    return;
                                }
                                RelativeLayout relativeLayout = (RelativeLayout) battleFragment.getView().findViewById(battleFragment.getPkmLayoutId(split[0]));
                                View v = relativeLayout.findViewWithTag("Substitute");
                                if (v != null) {
                                    relativeLayout.removeView(v);
                                }
                                ImageView imageView = (ImageView) battleFragment.getView().findViewById(battleFragment.getSpriteId(split[0]));
                                imageView.setAlpha(1f);
                            }
                        });
                        break;

                    case "uproar":
                        toAppendBuilder.append(attackerOutputName).append(" calmed down.");
                        break;

                    case "stockpile":
                        toAppendBuilder.append(attackerOutputName).append("'s stockpiled effect wore off!");
                        break;

                    case "infestation":
                        toAppendBuilder.append(attackerOutputName).append(" was freed from Infestation!");
                        break;

                    default:
                        if (split[1].contains("move:")) {
                            toAppendBuilder.append(attackerOutputName).append(" took the ").append(battleFragment.getPrintable(split[1])).append(" attack!");
                        } else {
                            toAppendBuilder.append(attackerOutputName).append("'s ").append(battleFragment.getPrintable(split[1])).append(" ended!");
                        }
                        break;
                }
                logMessage = new SpannableString(toAppendBuilder);
                toast = battleFragment.makeMinorToast(logMessage);
                animatorSet.play(toast);
                battleFragment.startAnimation(animatorSet, message);
                break;

            case "-singleturn":
                attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0]);
                switch (MyApplication.toId(battleFragment.getPrintable(split[1]))) {
                    case "roost":
                        toAppendBuilder.append(attackerOutputName).append(" landed on the ground!");
                        break;

                    case "quickguard":
                        attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0], false);
                        toAppendBuilder.append("Quick Guard protected ").append(attackerOutputName).append(" landed on the ground!");
                        break;

                    case "wideguard":
                        attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0], false);
                        toAppendBuilder.append("Wide Guard protected ").append(attackerOutputName).append(" landed on the ground!");
                        break;

                    case "protect":
                        toAppendBuilder.append(attackerOutputName).append(" protected itself!");
                        break;

                    case "endure":
                        toAppendBuilder.append(attackerOutputName).append(" braced itself!");
                        break;

                    case "helpinghand":
                        attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0], false);
                        toAppendBuilder.append(battleFragment.getPrintableOutputPokemonSide(ofSource)).append(" is ready to help ").append(attackerOutputName).append("!");
                        break;

                    case "focuspunch":
                        toAppendBuilder.append(attackerOutputName).append(" is tightening its focus!");
                        break;

                    case "snatch":
                        toAppendBuilder.append(attackerOutputName).append("  waits for a target to make a move!");
                        break;

                    case "magiccoat":
                        toAppendBuilder.append(attackerOutputName).append(" shrouded itself with Magic Coat!'");
                        break;

                    case "matblock":
                        toAppendBuilder.append(attackerOutputName).append(" intends to flip up a mat and block incoming attacks!");
                        break;

                    case "electrify":
                        toAppendBuilder.append(attackerOutputName).append("'s moves have been electrified!");
                        break;
                }
                logMessage = new SpannableString(toAppendBuilder);
                toast = battleFragment.makeMinorToast(logMessage);
                animatorSet = battleFragment.createFlyingMessage(split[0], toast, new SpannableString(battleFragment.getPrintable(split[1])));
                battleFragment.startAnimation(animatorSet, message);
                break;

            case "-singlemove":
                attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0]);
                switch (MyApplication.toId(battleFragment.getPrintable(split[1]))) {
                    case "grudge":
                        toAppendBuilder.append(attackerOutputName).append(" wants its target to bear a grudge!");
                        break;
                    case "destinybond":
                        toAppendBuilder.append(attackerOutputName).append(" is trying to take its foe down with it!");
                        break;
                }
                logMessage = new SpannableString(toAppendBuilder);
                toast = battleFragment.makeMinorToast(logMessage);
                animatorSet = battleFragment.createFlyingMessage(split[0], toast, new SpannableString(battleFragment.getPrintable(split[1])));
                battleFragment.startAnimation(animatorSet, message);
                break;

            case "-activate":
                attacker = split[0];
                attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0]);
                switch (MyApplication.toId(battleFragment.getPrintable(split[1]))) {
                    case "confusion":
                        toAppendBuilder.append(attackerOutputName).append(" is confused!");
                        break;

                    case "destinybond":
                        toAppendBuilder.append(attackerOutputName).append(" took its attacker down with it!");
                        break;

                    case "snatch":
                        toAppendBuilder.append(attackerOutputName).append(" snatched ").append(battleFragment.getPrintable(ofSource)).append("'s move!");
                        break;

                    case "grudge":
                        toAppendBuilder.append(attackerOutputName).append("'s").append(battleFragment.getPrintable(split[2])).append(" lost all its PP due to the grudge!");
                        break;

                    case "quickguard":
                        attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0], false);
                        toAppendBuilder.append("Quick Guard protected ").append(attackerOutputName).append("!");
                        break;

                    case "wideguard":
                        attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0], false);
                        toAppendBuilder.append("Wide Guard protected ").append(attackerOutputName).append("!");
                        break;

                    case "protect":
                        toAppendBuilder.append(attackerOutputName).append(" protected itself!");
                        break;

                    case "substitute":
                        if (messageDetails.contains("[damage]")) {
                            attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0], false);
                            toAppendBuilder.append("The substitute took damage for ").append(attackerOutputName).append(" protected itself!");
                        } else if (messageDetails.contains("[block]")) {
                            toAppendBuilder.append(attackerOutputName).append("'s Substitute blocked").append(battleFragment.getPrintable(split[2])).append("!");
                        }
                        break;

                    case "attract":
                        toAppendBuilder.append(attackerOutputName).append(" is in love with ").append(battleFragment.getPrintable(ofSource)).append("!");
                        break;

                    case "bide":
                        toAppendBuilder.append(attackerOutputName).append(" is storing energy!");
                        break;

                    case "mist":
                        toAppendBuilder.append(attackerOutputName).append(" is protected by the mist!");
                        break;

                    case "trapped":
                        toAppendBuilder.append(attackerOutputName).append(" can no longer escape!");
                        break;

                    case "stickyweb":
                        toAppendBuilder.append(attackerOutputName).append(" was caught in a sticky web!");
                        break;

                    case "happyhour":
                        toAppendBuilder.append("Everyone is caught up in the happy atmosphere!");
                        break;

                    case "celebrate":
                        if (attacker.startsWith("p2")) {
                            side = battleFragment.getPlayer2();
                        } else {
                            side = battleFragment.getPlayer1();
                        }
                        toAppendBuilder.append("Congratulations, ").append(side).append("!");
                        break;

                    case "trick":
                    case "switcheroo":
                        toAppendBuilder.append(attackerOutputName).append(" switched items with its target!");
                        break;

                    case "brickbreak":
                        if (MyApplication.toId(ofSource).startsWith("p2")) {
                            side = "the opposing team";
                        } else {
                            side = "your team";
                        }
                        toAppendBuilder.append(attackerOutputName).append(" shattered ").append(side).append(" protections!");
                        break;

                    case "pursuit":
                        toAppendBuilder.append(attackerOutputName).append(" is being sent back!");
                        break;

                    case "feint":
                        toAppendBuilder.append(attackerOutputName).append(" fell for the feint!");
                        break;

                    case "spite":
                        toAppendBuilder.append("It reduced the PP of ").append(attackerOutputName).append("'s ").append(battleFragment.getPrintable(split[2])).append(" by ").append(battleFragment.getPrintable(split[3])).append("!");
                        break;

                    case "gravity":
                        toAppendBuilder.append(attackerOutputName).append(" couldn't stay airborne because of gravity!");
                        break;

                    case "magnitude":
                        toAppendBuilder.append("Magnitude ").append(battleFragment.getPrintable(split[2])).append("!");
                        break;

                    case "sketch":
                        toAppendBuilder.append(attackerOutputName).append(" sketched ").append(battleFragment.getPrintable(split[2])).append("!");
                        break;

                    case "skillswap":
                        toAppendBuilder.append(attackerOutputName).append(" swapped Abilities with its target!");
                        if (ofSource != null) {
                            toAppendBuilder.append("\n").append(attackerOutputName).append(" acquired ").append(battleFragment.getPrintable(split[2])).append("!");
                            toAppendBuilder.append("\n").append(battleFragment.getPrintable(ofSource)).append(" acquired ").append(battleFragment.getPrintable(split[3])).append("!");
                        }
                        break;

                    case "charge":
                        toAppendBuilder.append(attackerOutputName).append(" began charging power!");
                        break;

                    case "struggle":
                        toAppendBuilder.append(attackerOutputName).append(" has no moves left!");
                        break;

                    case "bind":
                        toAppendBuilder.append(attackerOutputName).append(" was squeezed by ").append(battleFragment.getPrintable(ofSource)).append("!");
                        break;

                    case "wrap":
                        toAppendBuilder.append(attackerOutputName).append(" was wrapped by ").append(battleFragment.getPrintable(ofSource)).append("!");
                        break;

                    case "clamp":
                        toAppendBuilder.append(battleFragment.getPrintable(ofSource)).append(" clamped ").append(attackerOutputName).append("!");
                        break;

                    case "whirlpool":
                        toAppendBuilder.append(attackerOutputName).append(" became trapped in the vortex!");
                        break;

                    case "firespin":
                        toAppendBuilder.append(attackerOutputName).append(" became trapped in the fiery vortex!");
                        break;

                    case "magmastorm":
                        toAppendBuilder.append(attackerOutputName).append(" became trapped by swirling magma!");
                        break;

                    case "sandtomb":
                        toAppendBuilder.append(attackerOutputName).append(" became trapped by Sand Tomb!");
                        break;

                    case "infestation":
                        toAppendBuilder.append(attackerOutputName).append(" has been afflicted with an infestation by ").append(battleFragment.getPrintable(ofSource)).append("!");
                        break;

                    case "afteryou":
                        toAppendBuilder.append(attackerOutputName).append(" took the kind offer!");
                        break;

                    case "quash":
                        toAppendBuilder.append(attackerOutputName).append("'s move was postponed!");
                        break;

                    case "powersplit":
                        toAppendBuilder.append(attackerOutputName).append(" shared its power with the target!");
                        break;

                    case "guardsplit":
                        toAppendBuilder.append(attackerOutputName).append(" shared its guard with the target!");
                        break;

                    case "ingrain":
                        toAppendBuilder.append(attackerOutputName).append(" anchored itself with its roots!");
                        break;

                    case "matblock":
                        toAppendBuilder.append(battleFragment.getPrintable(split[2])).append(" was blocked by the kicked-up mat!");
                        break;

                    case "powder":
                        toAppendBuilder.append("When the flame touched the powder on the Pokémon, it exploded!");
                        break;

                    case "fairylock":
                        toAppendBuilder.append("No one will be able to run away during the next turn!");
                        break;

                    //abilities
                    case "sturdy":
                        toAppendBuilder.append(attackerOutputName).append(" held on thanks to Sturdy!");
                        break;

                    case "magicbounce":
                    case "magiccoat":
                    case "rebound":
                        break;

                    case "wonderguard":
                        toAppendBuilder.append(attackerOutputName).append("'s Wonder Guard evades the attack!");
                        break;

                    case "speedboost":
                        toAppendBuilder.append(attackerOutputName).append("'s' Speed Boost increases its speed!");
                        break;

                    case "forewarn":
                        toAppendBuilder.append(attackerOutputName).append("'s Forewarn alerted it to ").append(battleFragment.getPrintable(split[2])).append("!");
                        break;

                    case "anticipation":
                        toAppendBuilder.append(attackerOutputName).append(" shuddered!");
                        break;

                    case "telepathy":
                        toAppendBuilder.append(attackerOutputName).append(" avoids attacks by its ally Pok&#xE9;mon!");
                        break;

                    case "suctioncups":
                        toAppendBuilder.append(attackerOutputName).append(" anchors itself!");
                        break;

                    case "symbiosis":
                        attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0], false);
                        toAppendBuilder.append(battleFragment.getPrintable(ofSource)).append(" shared its ").append(battleFragment.getPrintable(split[2])).append(" with ").append(attackerOutputName);
                        break;

                    //items
                    case "custapberry":
                    case "quickclaw":
                        toAppendBuilder.append(attackerOutputName).append("'s ").append(battleFragment.getPrintable(split[1])).append(" let it move first!");
                        break;

                    case "leppaberry":
                        toAppendBuilder.append(attackerOutputName).append(" restored ").append(battleFragment.getPrintable(split[2])).append("'s PP using its Leppa Berry!");
                        break;

                    default:
                        toAppendBuilder.append(attackerOutputName).append("'s ").append(" activated!");
                        break;
                }
                logMessage = new SpannableString(toAppendBuilder);
                toast = battleFragment.makeMinorToast(logMessage);
                animatorSet = battleFragment.createFlyingMessage(split[0], toast, new SpannableString(battleFragment.getPrintable(split[1])));
                battleFragment.startAnimation(animatorSet, message);
                break;

            case "-sidestart":
                if (messageDetails.startsWith("p2")) {
                    side = "the opposing team";
                } else {
                    side = "your team";
                }

                fromEffect = split[1];
                fromEffectId = MyApplication.toId(battleFragment.getPrintable(fromEffect));
                animatorSet = new AnimatorSet();
                switch (fromEffectId) {
                    case "stealthrock":
                        toAppendBuilder.append("Pointed stones float in the air around ").append(side).append("!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                if (battleFragment.getView() == null) {
                                    return;
                                }
                                int id = (messageDetails.startsWith("p1")) ? R.id.field_rocks : R.id.field_rocks_o;
                                battleFragment.getView().findViewById(id).setVisibility(View.VISIBLE);
                            }
                        });
                        break;

                    case "spikes":
                        toAppendBuilder.append("Spikes were scattered all around the feet of ").append(side).append("!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                if (battleFragment.getView() == null) {
                                    return;
                                }
                                battleFragment.getView().findViewById(battleFragment.getLastVisibleSpike(messageDetails, true)).setVisibility(View.VISIBLE);
                            }
                        });
                        break;

                    case "toxicspikes":
                        toAppendBuilder.append("Toxic spikes were scattered all around the feet of ").append(side).append("!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                if (battleFragment.getView() == null) {
                                    return;
                                }
                                battleFragment.getView().findViewById(battleFragment.getLastVisibleTSpike(messageDetails, true)).setVisibility(View.VISIBLE);
                            }
                        });
                        break;

                    case "stickyweb":
                        toAppendBuilder.append("A sticky web spreads out beneath ").append(side).append("'s feet!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                if (battleFragment.getView() == null) {
                                    return;
                                }
                                int id = (messageDetails.startsWith("p1")) ? R.id.field_webs : R.id.field_webs_o;
                                battleFragment.getView().findViewById(id).setVisibility(View.VISIBLE);
                            }
                        });
                        break;

                    case "tailwind":
                        toAppendBuilder.append("The tailwind blew from behind ").append(side).append("!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                if (battleFragment.getView() == null) {
                                    return;
                                }
                                ((TextView) battleFragment.getView().findViewById(R.id.tail_wind)).setText("Tailwind");
                            }
                        });
                        break;

                    case "reflect":
                        toAppendBuilder.append("Reflect raised ").append(side).append("'s Defense!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                if (battleFragment.getView() == null) {
                                    return;
                                }
                                int id = (messageDetails.startsWith("p1")) ? R.id.field_reflect : R.id.field_reflect_o;
                                battleFragment.getView().findViewById(id).setVisibility(View.VISIBLE);
                            }
                        });
                        break;

                    case "lightscreen":
                        toAppendBuilder.append("Light Screen raised ").append(side).append("'s Special Defense!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                if (battleFragment.getView() == null) {
                                    return;
                                }
                                int id = (messageDetails.startsWith("p1")) ? R.id.field_lightscreen : R.id.field_lightscreen_o;
                                battleFragment.getView().findViewById(id).setVisibility(View.VISIBLE);
                            }
                        });
                        break;

                    case "auroraveil":
                        toAppendBuilder.append("Aurora Veil made the opposing team stronger against physical and special moves!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                if (battleFragment.getView() == null) {
                                    return;
                                }
                                int id = (messageDetails.startsWith("p1")) ? R.id.field_auroraveil : R.id.field_auroraveil_o;
                                battleFragment.getView().findViewById(id).setVisibility(View.VISIBLE);
                            }
                        });
                        break;

                    case "safeguard":
                        side = Character.toUpperCase(side.charAt(0)) + side.substring(1);
                        toAppendBuilder.append(side).append(" became cloaked in a mystical veil!");
                        break;

                    case "mist":
                        side = Character.toUpperCase(side.charAt(0)) + side.substring(1);
                        toAppendBuilder.append(side).append(" became shrouded in mist!");
                        break;

                    case "luckychant":
                        toAppendBuilder.append("The Lucky Chant shielded ").append(side).append(" from critical hits!");
                        break;

                    case "firepledge":
                        toAppendBuilder.append("A sea of fire enveloped ").append(side).append("!");
                        break;

                    case "waterpledge":
                        toAppendBuilder.append("A rainbow appeared in the sky on ").append(side).append("'s side!");
                        break;

                    case "grasspledge":
                        toAppendBuilder.append("A swamp enveloped ").append(side).append("!");
                        break;

                    default:
                        toAppendBuilder.append(battleFragment.getPrintable(fromEffect)).append(" started!");
                        break;
                }

                logMessage = new SpannableStringBuilder(toAppendBuilder);
                toast = battleFragment.makeMinorToast(logMessage);
                animatorSet.play(toast);
                battleFragment.startAnimation(animatorSet, message);
                break;

            case "-sideend":
                if (messageDetails.startsWith("p2")) {
                    side = "the opposing team";
                } else {
                    side = "your team";
                }

                fromEffect = split[1];
                fromEffectId = MyApplication.toId(battleFragment.getPrintable(fromEffect));

                animatorSet = new AnimatorSet();
                switch (fromEffectId) {
                    case "stealthrock":
                        toAppendBuilder.append("The pointed stones disappeared from around ").append(side).append("!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                if (battleFragment.getView() == null) {
                                    return;
                                }
                                int id = (messageDetails.startsWith("p1")) ? R.id.field_rocks : R.id.field_rocks_o;
                                battleFragment.getView().findViewById(id).setVisibility(View.INVISIBLE);
                            }
                        });
                        break;

                    case "spikes":
                        toAppendBuilder.append("The spikes disappeared from around ").append(side).append("!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                if (battleFragment.getView() == null) {
                                    return;
                                }
                                battleFragment.getView().findViewById(battleFragment.getLastVisibleSpike(messageDetails, false)).setVisibility(View.INVISIBLE);
                                battleFragment.getView().findViewById(battleFragment.getLastVisibleSpike(messageDetails, false)).setVisibility(View.INVISIBLE);
                                battleFragment.getView().findViewById(battleFragment.getLastVisibleSpike(messageDetails, false)).setVisibility(View.INVISIBLE);
                            }
                        });
                        break;

                    case "toxicspikes":
                        toAppendBuilder.append("The poison spikes disappeared from around ").append(side).append("!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                if (battleFragment.getView() == null) {
                                    return;
                                }
                                battleFragment.getView().findViewById(battleFragment.getLastVisibleTSpike(messageDetails, false)).setVisibility(View.INVISIBLE);
                                battleFragment.getView().findViewById(battleFragment.getLastVisibleTSpike(messageDetails, false)).setVisibility(View.INVISIBLE);
                            }
                        });
                        break;

                    case "stickyweb":
                        toAppendBuilder.append("The sticky web has disappeared from beneath ").append(side).append("'s feet!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                if (battleFragment.getView() == null) {
                                    return;
                                }
                                int id = (messageDetails.startsWith("p1")) ? R.id.field_webs : R.id.field_webs_o;
                                battleFragment.getView().findViewById(id).setVisibility(View.INVISIBLE);
                            }
                        });
                        break;

                    case "tailwind":
                        side = Character.toUpperCase(side.charAt(0)) + side.substring(1);
                        toAppendBuilder.append(side).append("'s tailwind petered out!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                if (battleFragment.getView() == null) {
                                    return;
                                }
                                ((TextView) battleFragment.getView().findViewById(R.id.tail_wind)).setText(null);
                            }
                        });
                        break;

                    case "reflect":
                        side = Character.toUpperCase(side.charAt(0)) + side.substring(1);
                        toAppendBuilder.append(side).append("'s Reflect wore off!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                if (battleFragment.getView() == null) {
                                    return;
                                }
                                int id = (messageDetails.startsWith("p1")) ? R.id.field_reflect : R.id.field_reflect_o;
                                battleFragment.getView().findViewById(id).setVisibility(View.INVISIBLE);
                            }
                        });
                        break;

                    case "lightscreen":
                        side = Character.toUpperCase(side.charAt(0)) + side.substring(1);
                        toAppendBuilder.append(side).append("'s Light Screen wore off!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                if (battleFragment.getView() == null) {
                                    return;
                                }
                                int id = (messageDetails.startsWith("p1")) ? R.id.field_lightscreen : R.id.field_lightscreen_o;
                                battleFragment.getView().findViewById(id).setVisibility(View.INVISIBLE);
                            }
                        });
                        break;

                    case "auroraveil":
                        side = Character.toUpperCase(side.charAt(0)) + side.substring(1);
                        toAppendBuilder.append(side).append("'s Aurora Veil wore off!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                if (battleFragment.getView() == null) {
                                    return;
                                }
                                int id = (messageDetails.startsWith("p1")) ? R.id.field_auroraveil : R.id.field_auroraveil_o;
                                battleFragment.getView().findViewById(id).setVisibility(View.INVISIBLE);
                            }
                        });
                        break;

                    case "safeguard":
                        side = Character.toUpperCase(side.charAt(0)) + side.substring(1);
                        toAppendBuilder.append(side).append(" is no longer protected by Safeguard!");
                        break;

                    case "mist":
                        side = Character.toUpperCase(side.charAt(0)) + side.substring(1);
                        toAppendBuilder.append(side).append(" is no longer protected by mist!");
                        break;

                    case "luckychant":
                        side = Character.toUpperCase(side.charAt(0)) + side.substring(1);
                        toAppendBuilder.append(side).append("'s Lucky Chant wore off!");
                        break;

                    case "firepledge":
                        toAppendBuilder.append("The sea of fire around ").append(side).append(" disappeared!");
                        break;

                    case "waterpledge":
                        toAppendBuilder.append("The rainbow on ").append(side).append("'s side disappeared!");
                        break;

                    case "grasspledge":
                        toAppendBuilder.append("The swamp around ").append(side).append(" disappeared!");
                        break;

                    default:
                        toAppendBuilder.append(battleFragment.getPrintable(fromEffect)).append(" ended!");
                        break;
                }

                logMessage = new SpannableString(toAppendBuilder);
                toast = battleFragment.makeMinorToast(logMessage);
                animatorSet.play(toast);
                battleFragment.startAnimation(animatorSet, message);
                break;

            case "-weather":
                String get;
                if (split[0].contains("ability")) {
                    get = split[1].substring(0, split[1].indexOf("|"));
                } else {
                    get = split[0];
                }


                boolean upkeep = false;
                if (split.length > 1 && split[1].contains("upkeep")) {
                    upkeep = true;
                }

                final String weather = get;

                animatorSet = new AnimatorSet();
                switch (weather) {
                    case "RainDance":
                        if (upkeep) {
                            toAppendBuilder.append("Rain continues to fall!");
                        } else {
                            toAppendBuilder.append("It started to rain!");
                            battleFragment.setWeatherExist(true);
                            animatorSet.addListener(new AnimatorListenerWithNet() {
                                @Override
                                public void onAnimationStartWithNet(Animator animation) {
                                    if (battleFragment.getView() == null) {
                                        return;
                                    }
                                    ((ImageView) battleFragment.getView().findViewById(R.id.weather_background)).setImageResource(R.drawable.weather_raindance);
                                    ((TextView) battleFragment.getView().findViewById(R.id.weather)).setText(weather);
                                }
                            });
                        }
                        break;
                    case "PrimordialSea":
                        if (upkeep) {
                            toAppendBuilder.append("There's no relief from this heavy rain!");
                        } else {
                            toAppendBuilder.append("A heavy rain began to fall!");
                            battleFragment.setWeatherExist(true);
                            animatorSet.addListener(new AnimatorListenerWithNet() {
                                @Override
                                public void onAnimationStartWithNet(Animator animation) {
                                    if (battleFragment.getView() == null) {
                                        return;
                                    }
                                    ((ImageView) battleFragment.getView().findViewById(R.id.weather_background)).setImageResource(R.drawable.weather_raindance);
                                    ((TextView) battleFragment.getView().findViewById(R.id.weather)).setText(weather);
                                }
                            });
                        }
                        break;
                    case "Sandstorm":
                        if (upkeep) {
                            toAppendBuilder.append("The sandstorm rages.");
                        } else {
                            toAppendBuilder.append("A sandstorm kicked up!");
                            battleFragment.setWeatherExist(true);
                            animatorSet.addListener(new AnimatorListenerWithNet() {
                                @Override
                                public void onAnimationStartWithNet(Animator animation) {
                                    if (battleFragment.getView() == null) {
                                        return;
                                    }
                                    ((ImageView) battleFragment.getView().findViewById(R.id.weather_background)).setImageResource(R.drawable.weather_sandstorm);
                                    ((TextView) battleFragment.getView().findViewById(R.id.weather)).setText(weather);
                                }
                            });
                        }
                        break;
                    case "SunnyDay":
                        if (upkeep) {
                            toAppendBuilder.append("The sunlight is strong!");
                        } else {
                            toAppendBuilder.append("The sunlight turned harsh!");
                            battleFragment.setWeatherExist(true);
                            animatorSet.addListener(new AnimatorListenerWithNet() {
                                @Override
                                public void onAnimationStartWithNet(Animator animation) {
                                    if (battleFragment.getView() == null) {
                                        return;
                                    }
                                    ((ImageView) battleFragment.getView().findViewById(R.id.weather_background)).setImageResource(R.drawable.weather_sunnyday);
                                    ((TextView) battleFragment.getView().findViewById(R.id.weather)).setText(weather);
                                }
                            });
                        }
                        break;
                    case "DesolateLand":
                        if (upkeep) {
                            toAppendBuilder.append("The extremely harsh sunlight was not lessened at all!");
                        } else {
                            toAppendBuilder.append("The sunlight turned extremely harsh!");
                            battleFragment.setWeatherExist(true);
                            animatorSet.addListener(new AnimatorListenerWithNet() {
                                @Override
                                public void onAnimationStartWithNet(Animator animation) {
                                    if (battleFragment.getView() == null) {
                                        return;
                                    }
                                    ((ImageView) battleFragment.getView().findViewById(R.id.weather_background)).setImageResource(R.drawable.weather_sunnyday);
                                    ((TextView) battleFragment.getView().findViewById(R.id.weather)).setText(weather);
                                }
                            });
                        }
                        break;
                    case "Hail":
                        if (upkeep) {
                            toAppendBuilder.append("The hail crashes down.");
                        } else {
                            toAppendBuilder.append("It started to hail!");
                            battleFragment.setWeatherExist(true);
                            animatorSet.addListener(new AnimatorListenerWithNet() {
                                @Override
                                public void onAnimationStartWithNet(Animator animation) {
                                    if (battleFragment.getView() == null) {
                                        return;
                                    }
                                    ((ImageView) battleFragment.getView().findViewById(R.id.weather_background)).setImageResource(R.drawable.weather_hail);
                                    ((TextView) battleFragment.getView().findViewById(R.id.weather)).setText(weather);
                                }
                            });
                        }
                        break;
                    case "DeltaStream":
                        if (upkeep) {
                            toAppendBuilder.append("The mysterious air current blows on regardless!");
                        } else {
                            toAppendBuilder.append("A mysterious air current starts to blow!");
                            battleFragment.setWeatherExist(true);
                            animatorSet.addListener(new AnimatorListenerWithNet() {
                                @Override
                                public void onAnimationStartWithNet(Animator animation) {
                                    if (battleFragment.getView() == null) {
                                        return;
                                    }
                                    ((ImageView) battleFragment.getView().findViewById(R.id.weather_background)).setImageResource(R.drawable.weather_strongwind);
                                    ((TextView) battleFragment.getView().findViewById(R.id.weather)).setText(weather);
                                }
                            });
                        }
                        break;
                    case "none":
                        if (battleFragment.isWeatherExist()) {
                            switch (battleFragment.getCurrentWeather()) {
                                case "RainDance":
                                    toAppendBuilder.append("The rain stopped.");
                                    break;
                                case "PrimordialSea":
                                    toAppendBuilder.append("The heavy rain has lifted!");
                                    break;
                                case "SunnyDay":
                                    toAppendBuilder.append("The sunlight faded.");
                                    break;
                                case "DesolateLand":
                                    toAppendBuilder.append("The harsh sunlight faded.");
                                    break;
                                case "Sandstorm":
                                    toAppendBuilder.append("The sandstorm subsided.");
                                    break;
                                case "Hail":
                                    toAppendBuilder.append("The hail stopped.");
                                    break;
                                case "DeltaStream":
                                    toAppendBuilder.append("The mysterious wind stopped.");
                            }
                            animatorSet.addListener(new AnimatorListenerWithNet() {
                                @Override
                                public void onAnimationStartWithNet(Animator animation) {
                                    if (battleFragment.getView() == null) {
                                        return;
                                    }
                                    ((ImageView) battleFragment.getView().findViewById(R.id.weather_background)).setImageResource(0);
                                    ((TextView) battleFragment.getView().findViewById(R.id.weather)).setText(null);
                                }
                            });
                        }
                        battleFragment.setWeatherExist(false);
                        break;
                }
                battleFragment.setCurrentWeather(weather);
                logMessage = new SpannableString(toAppendBuilder);
                toast = battleFragment.makeMinorToast(logMessage);
                animatorSet.play(toast);
                battleFragment.startAnimation(animatorSet, message);
                break;

            case "-fieldstart":
                attackerOutputName = ofSource;
                animatorSet = new AnimatorSet();
                switch (MyApplication.toId(battleFragment.getPrintable(split[0]))) {
                    case "trickroom":
                        toAppendBuilder.append(attackerOutputName).append(" twisted the dimensions!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                if (battleFragment.getView() == null) {
                                    return;
                                }
                                ((ImageView) battleFragment.getView().findViewById(R.id.battle_background)).setImageResource(R.drawable.weather_trickroom);
                            }
                        });
                        break;

                    case "wonderroom":
                        toAppendBuilder.append("It created a bizarre area in which the Defense and Sp. Def BattleFragment.stats are swapped!");
                        break;

                    case "magicroom":
                        toAppendBuilder.append("It created a bizarre area in which Pok&#xE9;mon's held items lose their effects!");
                        break;

                    case "gravity":
                        toAppendBuilder.append("Gravity intensified!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                if (battleFragment.getView() == null) {
                                    return;
                                }
                                ((TextView) battleFragment.getView().findViewById(R.id.gra_vity)).setText("Gravity");
                            }
                        });
                        break;

                    case "mudsport":
                        toAppendBuilder.append("Electric's power was weakened!");
                        break;

                    case "watersport":
                        toAppendBuilder.append("Fire's power was weakened!");
                        break;

                    case "electricterrain":
                        toAppendBuilder.append("An electric current runs across the battlefield!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                if (battleFragment.getView() == null) {
                                    return;
                                }
                                ((ImageView) battleFragment.getView().findViewById(R.id.battle_terrain)).setImageResource(R.drawable.terrain_electric);
                            }
                        });
                        break;

                    case "psychicterrain":
                        toAppendBuilder.append("The battlefield got weird!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                if (battleFragment.getView() == null) {
                                    return;
                                }
                                ((ImageView) battleFragment.getView().findViewById(R.id.battle_terrain)).setImageResource(R.drawable.terrain_psychic);
                            }
                        });
                        break;

                    case "grassyterrain":
                        toAppendBuilder.append("Grass grew to cover the battlefield!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                if (battleFragment.getView() == null) {
                                    return;
                                }
                                ((ImageView) battleFragment.getView().findViewById(R.id.battle_terrain)).setImageResource(R.drawable.terrain_grassy);
                            }
                        });
                        break;

                    case "mistyterrain":
                        toAppendBuilder.append("Mist swirls around the battlefield!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                if (battleFragment.getView() == null) {
                                    return;
                                }
                                ((ImageView) battleFragment.getView().findViewById(R.id.battle_terrain)).setImageResource(R.drawable.terrain_misty);
                            }
                        });
                        break;

                    default:
                        toAppendBuilder.append(battleFragment.getPrintable(split[0])).append(" started!");
                        break;
                }
                logMessage = new SpannableString(toAppendBuilder);
                toast = battleFragment.makeMinorToast(logMessage);
                animatorSet.play(toast);
                battleFragment.startAnimation(animatorSet, message);
                break;

            case "-fieldend":
                animatorSet = new AnimatorSet();
                switch (MyApplication.toId(battleFragment.getPrintable(split[0]))) {
                    case "trickroom":
                        toAppendBuilder.append("The twisted dimensions returned to normal!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                if (battleFragment.getView() == null) {
                                    return;
                                }
                                int id = new Random().nextInt(BattleFragment.BACKGROUND_LIBRARY.length);
                                ((ImageView) battleFragment.getView().findViewById(R.id.battle_background)).setImageResource(BattleFragment.BACKGROUND_LIBRARY[id]);
                            }
                        });
                        break;

                    case "wonderroom":
                        toAppendBuilder.append("'Wonder Room wore off, and the Defense and Sp. Def BattleFragment.stats returned to normal!");
                        break;

                    case "magicroom":
                        toAppendBuilder.append("Magic Room wore off, and the held items' effects returned to normal!");
                        break;

                    case "gravity":
                        toAppendBuilder.append("Gravity returned to normal!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                if (battleFragment.getView() == null) {
                                    return;
                                }
                                ((TextView) battleFragment.getView().findViewById(R.id.gra_vity)).setText(null);
                            }
                        });
                        break;

                    case "mudsport":
                        toAppendBuilder.append("The effects of Mud Sport have faded.");
                        break;

                    case "watersport":
                        toAppendBuilder.append("The effects of Water Sport have faded.");
                        break;

                    case "electricterrain":
                        toAppendBuilder.append("The battlefield returned to normal.");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                if (battleFragment.getView() == null) {
                                    return;
                                }
                                ((ImageView) battleFragment.getView().findViewById(R.id.battle_terrain)).setImageResource(0);
                            }
                        });
                        break;

                    case "psychicterrain":
                        toAppendBuilder.append("The battlefield returned to normal.");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                if (battleFragment.getView() == null) {
                                    return;
                                }
                                ((ImageView) battleFragment.getView().findViewById(R.id.battle_terrain)).setImageResource(0);
                            }
                        });
                        break;

                    case "grassyterrain":
                        toAppendBuilder.append("The grass disappeared from the battlefield.");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                if (battleFragment.getView() == null) {
                                    return;
                                }
                                ((ImageView) battleFragment.getView().findViewById(R.id.battle_terrain)).setImageResource(0);
                            }
                        });
                        break;

                    case "mistyterrain":
                        toAppendBuilder.append("The battlefield returned to normal.");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                if (battleFragment.getView() == null) {
                                    return;
                                }
                                ((ImageView) battleFragment.getView().findViewById(R.id.battle_terrain)).setImageResource(0);
                            }
                        });
                        break;

                    default:
                        toAppendBuilder.append(battleFragment.getPrintable(split[0])).append(" ended!");
                        break;
                }
                logMessage = new SpannableString(toAppendBuilder);
                toast = battleFragment.makeMinorToast(logMessage);
                animatorSet.play(toast);
                battleFragment.startAnimation(animatorSet, message);
                break;

            case "-fieldactivate":
                switch (MyApplication.toId(battleFragment.getPrintable(split[0]))) {
                    case "perishsong":
                        toAppendBuilder.append("All Pok&#xE9;mon hearing the song will faint in three turns!");
                        break;

                    case "payday":
                        toAppendBuilder.append("Coins were scattered everywhere!");
                        break;

                    case "iondeluge":
                        toAppendBuilder.append("A deluge of ions showers the battlefield!");
                        break;

                    default:
                        toAppendBuilder.append(battleFragment.getPrintable(split[1])).append(" hit!");
                        break;
                }
                logMessage = new SpannableString(toAppendBuilder);
                toast = battleFragment.makeMinorToast(logMessage);
                battleFragment.startAnimation(toast, message);
                break;

            case "-message":
            case "-hint":
                logMessage = new SpannableString(messageDetails);
                toast = battleFragment.makeMinorToast(logMessage);
                battleFragment.startAnimation(toast, message);
                break;

            case "-anim":
                logMessage = new SpannableString(command + ":" + messageDetails);
                toast = battleFragment.makeMinorToast(logMessage);
                battleFragment.startAnimation(toast, message);
                break;

            default:
                toAppendSpannable = new SpannableString(command + ":" + messageDetails);
                toast = battleFragment.makeMinorToast(toAppendSpannable);
                battleFragment.startAnimation(toast, message);
                logMessage = new SpannableString(command + ":" + messageDetails);
                break;
        }

        if (messageDetails.contains("[silent]")) {
            return;
        }

        logMessage.setSpan(new RelativeSizeSpan(0.8f), 0, logMessage.toString().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        battleFragment.addToLog(logMessage);
    }

    public static int getGenderSprite(String gender) {
        if (gender == null) {
            return 0;
        }

        switch (gender) {
            case "M":
                return R.drawable.ic_gender_male;
            case "F":
                return R.drawable.ic_gender_female;
            default:
                return 0;
        }
    }

    private static String processSpecialName(String name) {
        for (String sp : BattleFragment.MORPHS) {
            if (name.contains(sp)) {
                return sp;
            }
        }
        return name;
    }

    private static void processPokemonDetailString(PokemonInfo pkm, String details) {
        int separator = details.indexOf(",");
        String name = (separator == -1) ? details : details.substring(0, separator);
        pkm.setName(name);
        if (details.contains(", L")) {
            String level = details.substring(details.indexOf(", L") + 3);
            level = !level.contains(",") ? level : level.substring(0, level.indexOf(","));
            pkm.setLevel(Integer.parseInt(level));
        }
        if (details.contains(", M")) {
            pkm.setGender("M");
        } else {
            if (details.contains(", F")) {
                pkm.setGender("F");
            }
        }
        if (details.contains("shiny")) {
            pkm.setShiny(true);
        }
    }

    public static PokemonInfo parsePokemonInfo(BattleFragment battleFragment, JSONObject info) throws JSONException {
        String details = info.getString("details");
        String name = !details.contains(",") ? details : details.substring(0, details.indexOf(","));
        PokemonInfo pkm = new PokemonInfo(battleFragment.getActivity(), name);
        processPokemonDetailString(pkm, details);
        String nickname = info.getString("ident").substring(4);
        pkm.setNickname(nickname);
        String hp = info.getString("condition");
        pkm.setHp(processHpFraction(hp));
        pkm.setStatus(processStatusFraction(hp));
        pkm.setActive(info.getBoolean("active"));
        JSONObject statsArray = info.getJSONObject("stats");
        int[] stats = new int[5];
        stats[0] = statsArray.getInt("atk");
        stats[1] = statsArray.getInt("def");
        stats[2] = statsArray.getInt("spa");
        stats[3] = statsArray.getInt("spd");
        stats[4] = statsArray.getInt("spe");
        pkm.setStats(stats);
        JSONArray movesArray = info.getJSONArray("moves");
        HashMap<String, Integer> moves = new HashMap<>();
        for (int i = 0; i < movesArray.length(); i++) {
            String move = movesArray.getString(i);
            if (move.startsWith("hiddenpower")) {
                move = move.toLowerCase().replaceAll("[^a-z]", "");
                //dirty fix to remvoe that 60 from hiddenpower...
            }
            moves.put(move, Integer.parseInt(MoveDex.getMoveMaxPP(battleFragment.getActivity(), move)));
        }
        pkm.setMoves(moves);
        pkm.setAbility(info.getString("baseAbility"));
        pkm.setItem(info.getString("item"));
        pkm.setCanMegaEvo(info.optBoolean("canMegaEvo", false));
        return pkm;
    }

    private static int processHpFraction(String hpFraction) {
        int status = hpFraction.indexOf(' ');
        hpFraction = (status == -1) ? hpFraction : hpFraction.substring(0, status);
        int fraction = hpFraction.indexOf('/');
        if (fraction == -1) {
            return 0;
        } else {
            int remaining = Integer.parseInt(hpFraction.substring(0, fraction));
            String totalString = hpFraction.substring(fraction + 1);
            if(totalString.indexOf('y') != -1 || totalString.indexOf('g') != -1) {
                // remopving last character, wtf zarel
                totalString = totalString.substring(0, totalString.length() - 1);
            }
            int total = Integer.parseInt(totalString);
            int toReturn = (int) (((float) remaining / (float) total) * 100);
            if (toReturn == 0 && remaining != 0) {
                return 1;
            }
            return toReturn;
        }
    }

    private static String processStatusFraction(String statusFraction) {
        int status = statusFraction.indexOf(' ');
        if (status == -1) {
            return null;
        } else {
            return statusFraction.substring(status + 1);
        }
    }
    public static void processMajorActionZHCN(final BattleFragment battleFragment, final String message)
            throws JSONException {
        BattleFieldData.RoomData roomData = BattleFieldData.get(battleFragment.getActivity()).getAnimationInstance(battleFragment.getRoomId());
        final BattleFieldData.ViewData viewData = BattleFieldData.get(battleFragment.getActivity()).getViewData(battleFragment.getRoomId());
        String command = (message.indexOf('|') == -1) ? message : message.substring(0, message.indexOf('|'));
        final String messageDetails = message.substring(message.indexOf('|') + 1);
        if (command.startsWith("-")) {
            processMinorActionZHCN(battleFragment, command, messageDetails, message);
            return;
        }

        int separator = messageDetails.indexOf('|');
        final String[] split = messageDetails.split("\\|");
        final ArrayList<PokemonInfo> team1 = battleFragment.getPlayer1Team();
        final ArrayList<PokemonInfo> team2 = battleFragment.getPlayer2Team();

        final ArrayList<PokemonInfo> team;
        final String position, attacker;
        final int iconId;
        final PokemonInfo pokemonInfo;
        int start;
        String remaining;
        final String toAppend;
        StringBuilder toAppendBuilder;
        Spannable toAppendSpannable;
        AnimatorSet toast;
        AnimatorSet animatorSet;
        Animator animator;

        Spannable logMessage = new SpannableString("");
        switch (command) {
            case "deinit":
                return;
            case "askreg":
                toast = battleFragment.makeToast(battleFragment.getResources().getString(R.string.ask_registration));
                battleFragment.startAnimation(toast, message);
                logMessage = new SpannableString(messageDetails);
                break;
            case "title":
            case "J":
            case "L":
            case "spectator":
            case "spectatorLeave":
                break;

            case "join":
            case "j":
                break;

            case "leave":
            case "l":
                break;

            case "chat":
            case "c":
            case "tc":
            case "c:":
                String user = split[0];
                String userMessage = split[1];
                toAppend = user + ": " + userMessage;
                toAppendSpannable = new SpannableString(toAppend);
                toAppendSpannable.setSpan(new ForegroundColorSpan(ChatRoomFragment.getColorStrong(user)),
                        0, user.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                logMessage = new SpannableString(toAppendSpannable);
                battleFragment.makeChatToast(user, userMessage);
                break;

            case "raw":
            case "variation":
            case "chatmsg":
            case "chatmsg-raw":
            case "html":
                toast = battleFragment.makeToast(Html.fromHtml(messageDetails).toString());
                battleFragment.startAnimation(toast, message);
                logMessage = new SpannableString(Html.fromHtml(messageDetails).toString());
                break;

            case "message":
                toast = battleFragment.makeToast(messageDetails);
                battleFragment.startAnimation(toast, message);
                logMessage = new SpannableString(messageDetails);
                break;

            case "gametype":
                if("singles".equals(messageDetails)) {
                    battleFragment.setSingles(true);
                } else {
                    battleFragment.setSingles(false);
                }
                break;
            case "gen":
                break;

            case "player":
                final String playerType;
                final String playerName;
                String avatar;
                if (separator == -1) {
                    playerType = messageDetails;
                    playerName = "";
                    avatar = null;
                } else {
                    playerType = split[0];
                    playerName = split[1];
                    avatar = split[2];
                    while (avatar.length() < 3) {
                        avatar = "0" + avatar;
                    }
                }
                final int avatarResource;
                if (avatar != null) {
                    avatarResource = battleFragment.getActivity().getApplicationContext()
                            .getResources().getIdentifier("avatar_" + avatar, "drawable",
                                    battleFragment.getActivity().getApplicationContext().getPackageName());

                } else {
                    avatarResource = 0;
                }
                if (playerType.equals("p1")) {
                    roomData.setPlayer1(playerName);
                    battleFragment.getActivity().runOnUiThread(new RunWithNet() {
                        @Override
                        public void runWithNet() {
                            if (battleFragment.getView() == null) {
                                viewData.addViewSetterOnHold(R.id.username, playerName,
                                        BattleFieldData.ViewData.SetterType.TEXTVIEW_SETTEXT);
                                viewData.addViewSetterOnHold(R.id.avatar, avatarResource,
                                        BattleFieldData.ViewData.SetterType.IMAGEVIEW_SETIMAGERESOURCE);
                            } else {
                                ((TextView) battleFragment.getView().findViewById(R.id.username)).setText(playerName);
                                ((ImageView) battleFragment.getView().findViewById(R.id.avatar)).setImageResource(avatarResource);
                            }
                        }
                    });
                    battleFragment.setPlayer1(playerName);
                } else {
                    roomData.setPlayer2(playerName);
                    battleFragment.getActivity().runOnUiThread(new RunWithNet() {
                        @Override
                        public void runWithNet() {
                            if (battleFragment.getView() == null) {
                                viewData.addViewSetterOnHold(R.id.username_o, playerName,
                                        BattleFieldData.ViewData.SetterType.TEXTVIEW_SETTEXT);
                                viewData.addViewSetterOnHold(R.id.avatar_o, avatarResource,
                                        BattleFieldData.ViewData.SetterType.IMAGEVIEW_SETIMAGERESOURCE);
                            } else {
                                ((TextView) battleFragment.getView().findViewById(R.id.username_o)).setText(playerName);
                                ((ImageView) battleFragment.getView().findViewById(R.id.avatar_o)).setImageResource(avatarResource);
                            }
                        }
                    });
                    battleFragment.setPlayer2(playerName);
                }
                break;

            case "tier":
                toAppend = "分级：" + "\n" + messageDetails;
                toAppendSpannable = new SpannableString(toAppend);
                toAppendSpannable.setSpan(new StyleSpan(Typeface.BOLD),
                        toAppend.indexOf('\n') + 1, toAppend.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                logMessage = new SpannableString(toAppendSpannable);
                break;

            case "rated":
                toAppend = command.toUpperCase();
                toAppendSpannable = new SpannableString(toAppend);
                toAppendSpannable.setSpan(new ForegroundColorSpan(R.color.dark_blue),
                        0, toAppend.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                logMessage = new SpannableString(toAppendSpannable);
                break;

            case "rule":
                toAppendSpannable = new SpannableString(messageDetails);
                toAppendSpannable.setSpan(new StyleSpan(Typeface.ITALIC),
                        0, messageDetails.indexOf(':') + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                logMessage = new SpannableString(toAppendSpannable);
                break;

            case "choice":
            case "":
                logMessage = new SpannableString(" ");
                break;

            case "clearpoke":
                if (battleFragment.getPlayer1Team() == null) {
                    battleFragment.setPlayer1Team(new ArrayList<PokemonInfo>());
                }
                battleFragment.setPlayer2Team(new ArrayList<PokemonInfo>());
                break;

            case "poke":
                playerType = split[0];
                int comma = split[1].indexOf(',');
                final String pokeName = (comma == -1) ? split[1] : split[1].substring(0, comma);
                team = (playerType.equals("p1")) ? team1 : team2;
                iconId = battleFragment.getIconId(playerType, team.size());
                pokemonInfo = new PokemonInfo(battleFragment.getActivity(), pokeName);
                processPokemonDetailString(pokemonInfo, split[1]);
                if (battleFragment.findPokemonInTeam(team,
                        pokemonInfo.getName()) == -1) {
                    team.add(pokemonInfo);

                    battleFragment.getActivity().runOnUiThread(new RunWithNet() {
                        @Override
                        public void runWithNet() {
                            int imageResource = Pokemon.getPokemonIcon(battleFragment.getActivity(),
                                    MyApplication.toId(pokeName));
                            if (battleFragment.getView() == null) {
                                viewData.addViewSetterOnHold(iconId, imageResource,
                                        BattleFieldData.ViewData.SetterType.IMAGEVIEW_SETIMAGERESOURCE);
                            } else {
                                ImageView icon = (ImageView) battleFragment.getView().findViewById(iconId);
                                if (icon != null) {
                                    icon.setImageResource(imageResource);
                                }
                            }
                        }
                    });
                }
                break;

            case "teampreview":
                int teamSelectionSize;
                try {
                    teamSelectionSize = Integer.parseInt(messageDetails);
                    // sometimes messageDetails is the number of mons (for VGC), sometimes it's just teampreview
                } catch (NumberFormatException e) {
                    teamSelectionSize = 0;
                }

                final ArrayList<PokemonInfo> t1 = battleFragment.getPlayer1Team();
                final ArrayList<PokemonInfo> t2 = battleFragment.getPlayer2Team();

                final int teamSelectionSizeFinal = teamSelectionSize;
                battleFragment.getActivity().runOnUiThread(new RunWithNet() {
                    @Override
                    public void runWithNet() {
                        if (battleFragment.getView() == null) {
                            return;
                        }

                        FrameLayout frameLayout = (FrameLayout) battleFragment.getView().findViewById(R.id.battle_interface);
                        frameLayout.removeAllViews();
                        battleFragment.getActivity().getLayoutInflater().inflate(R.layout.fragment_battle_teampreview, frameLayout);
                        for (int i = 0; i < t1.size(); i++) {
                            ImageView sprites = (ImageView) battleFragment.getView().findViewById(battleFragment.getTeamPreviewSpriteId("p1", i));
                            PokemonInfo pkm = t1.get(i);
                            sprites.setImageResource(Pokemon.getPokemonSprite(battleFragment.getActivity(),
                                    MyApplication.toId(pkm.getName()), true, pkm.isFemale(), pkm.isShiny()));
                            ((ImageView) battleFragment.getView().findViewById(battleFragment.getIconId("p1", i)))
                                    .setImageResource(Pokemon.getPokemonIcon(battleFragment.getActivity(),
                                            MyApplication.toId(pkm.getName())));
                        }
                        for (int i = t1.size(); i < 6; i++) {
                            ((ImageView) battleFragment.getView().findViewById(battleFragment.getIconId("p1", i)))
                                    .setImageResource(R.drawable.pokeball_none);
                        }
                        for (int i = 0; i < t2.size(); i++) {
                            ImageView sprites = (ImageView) battleFragment.getView().findViewById(battleFragment.getTeamPreviewSpriteId("p2", i));
                            PokemonInfo pkm = t2.get(i);
                            sprites.setImageResource(Pokemon.getPokemonSprite(battleFragment.getActivity(),
                                    MyApplication.toId(pkm.getName()), false, pkm.isFemale(), pkm.isShiny()));
                            ((ImageView) battleFragment.getView().findViewById(battleFragment.getIconId("p2", i)))
                                    .setImageResource(Pokemon.getPokemonIcon(battleFragment.getActivity(),
                                            MyApplication.toId(pkm.getName())));
                        }
                        for (int i = t2.size(); i < 6; i++) {
                            ((ImageView) battleFragment.getView().findViewById(battleFragment.getIconId("p2", i)))
                                    .setImageResource(R.drawable.pokeball_none);
                        }
                        battleFragment.setTeamSize(teamSelectionSizeFinal);
                        battleFragment.getView().findViewById(R.id.p2a_prev)
                                .setOnClickListener(battleFragment.new PokemonInfoListener(false, 0));
                        battleFragment.getView().findViewById(R.id.p2b_prev)
                                .setOnClickListener(battleFragment.new PokemonInfoListener(false, 1));
                        battleFragment.getView().findViewById(R.id.p2c_prev)
                                .setOnClickListener(battleFragment.new PokemonInfoListener(false, 2));
                        battleFragment.getView().findViewById(R.id.p2d_prev)
                                .setOnClickListener(battleFragment.new PokemonInfoListener(false, 3));
                        battleFragment.getView().findViewById(R.id.p2e_prev)
                                .setOnClickListener(battleFragment.new PokemonInfoListener(false, 4));
                        battleFragment.getView().findViewById(R.id.p2f_prev)
                                .setOnClickListener(battleFragment.new PokemonInfoListener(false, 5));

                        if (battleFragment.getAnimatorSetQueue().isEmpty() && battleFragment.getRequestJson() != null) {
                            battleFragment.startRequest();
                        }

                    }
                });

                toAppendBuilder = new StringBuilder();
                toAppendBuilder.append(battleFragment.getPlayer1()).append("'s Team: ");
                String[] p1Team = battleFragment.getTeamNameStringArray(team1);
                for (int i = 0; i < p1Team.length - 1; i++) {
                    toAppendBuilder.append(p1Team[i]).append("/");
                }
                toAppendBuilder.append(p1Team[p1Team.length - 1]);

                toAppendBuilder.append("\n").append(battleFragment.getPlayer2()).append("'s Team: ");
                String[] p2Team = battleFragment.getTeamNameStringArray(team2);
                for (int i = 0; i < p2Team.length - 1; i++) {
                    toAppendBuilder.append(p2Team[i]).append("/");
                }
                toAppendBuilder.append(p2Team[p2Team.length - 1]);
                toAppendSpannable = new SpannableStringBuilder(toAppendBuilder);
                logMessage = new SpannableString(toAppendSpannable);
                break;

            case "request":
                if (messageDetails.equals("null")) {
                    return;
                }
                JSONObject requestJson = new JSONObject(messageDetails);

                if (battleFragment.getBattling() == 0) {
                    battleFragment.setBattling(1);
                    battleFragment.setUpTimer();
                }

                battleFragment.setRequestJson(requestJson);
                battleFragment.setUndoMessage(requestJson);
                break;

            case "inactive":
                final String inactive;
                if (messageDetails.contains(" seconds left")) {
                    remaining = messageDetails.substring(0, messageDetails.indexOf(" seconds left"));
                    inactive = remaining.substring(remaining.lastIndexOf(' ')) + "s";

                    battleFragment.getActivity().runOnUiThread(new RunWithNet() {
                        @Override
                        public void runWithNet() {
                            if (battleFragment.getView() == null) {
                                viewData.addViewSetterOnHold(R.id.inactive, inactive,
                                        BattleFieldData.ViewData.SetterType.TEXTVIEW_SETTEXT);
                                viewData.addViewSetterOnHold(R.id.inactive, null,
                                        BattleFieldData.ViewData.SetterType.VIEW_VISIBLE);
                            } else {
                                TextView textView = (TextView) battleFragment.getView().findViewById(R.id.inactive);
                                textView.setVisibility(View.VISIBLE);
                                textView.setText(inactive);
                            }
                        }
                    });
                }
                toAppendSpannable = new SpannableString(messageDetails);
                toAppendSpannable.setSpan(new ForegroundColorSpan(R.color.dark_red),
                        0, messageDetails.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                logMessage = new SpannableString(toAppendSpannable);
                break;

            case "inactiveoff":
                battleFragment.getActivity().runOnUiThread(new RunWithNet() {
                    @Override
                    public void runWithNet() {
                        if (battleFragment.getView() == null) {
                            viewData.addViewSetterOnHold(R.id.inactive, null,
                                    BattleFieldData.ViewData.SetterType.VIEW_GONE);
                        } else {
                            battleFragment.getView().findViewById(R.id.inactive).setVisibility(View.GONE);
                        }
                    }
                });
                toAppendSpannable = new SpannableString(messageDetails);
                toAppendSpannable.setSpan(new ForegroundColorSpan(R.color.dark_red),
                        0, messageDetails.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                logMessage = new SpannableString(toAppendSpannable);
                break;

            case "start":
                if (Onboarding.get(battleFragment.getActivity()).isAdvertising()) {
                    // sending advertisement message
                    String advertisement = battleFragment.getRoomId() + "|" + battleFragment.getActivity().getString(R.string.advertise_message);
                    MyApplication.getMyApplication().sendClientMessage(advertisement);
                }
                battleFragment.getActivity().runOnUiThread(new RunWithNet() {
                    @Override
                    public void runWithNet() {
                        if (battleFragment.getView() == null) {
                            viewData.addViewSetterOnHold(R.id.battle_interface, null,
                                    BattleFieldData.ViewData.SetterType.BATTLE_START);
                        } else {
                            FrameLayout frameLayout = (FrameLayout) battleFragment.getView().findViewById(R.id.battle_interface);
                            frameLayout.removeAllViews();
                            battleFragment.getActivity().getLayoutInflater()
                                    .inflate(R.layout.fragment_battle_animation, frameLayout);
                            battleFragment.getView().findViewById(R.id.p1a)
                                    .setOnClickListener(battleFragment.new PokemonInfoListener(true, 0));
                            battleFragment.getView().findViewById(R.id.p1b)
                                    .setOnClickListener(battleFragment.new PokemonInfoListener(true, 1));
                            battleFragment.getView().findViewById(R.id.p1c)
                                    .setOnClickListener(battleFragment.new PokemonInfoListener(true, 2));
                            battleFragment.getView().findViewById(R.id.p2a)
                                    .setOnClickListener(battleFragment.new PokemonInfoListener(false, 0));
                            battleFragment.getView().findViewById(R.id.p2b)
                                    .setOnClickListener(battleFragment.new PokemonInfoListener(false, 1));
                            battleFragment.getView().findViewById(R.id.p2c)
                                    .setOnClickListener(battleFragment.new PokemonInfoListener(false, 2));
                        }
                    }
                });
                toAppend = battleFragment.getPlayer1() + " vs. " + battleFragment.getPlayer2();
                toAppendSpannable = new SpannableString(toAppend);
                toAppendSpannable.setSpan(new StyleSpan(Typeface.BOLD),
                        0, toAppend.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                logMessage = new SpannableString(toAppendSpannable);
                break;

            case "move":
                attacker = split[0].substring(5);
                String attackerTrans=translatePokemon(attacker);
                toAppendBuilder = new StringBuilder();
                if (messageDetails.startsWith("p2")) {
                    toAppendBuilder.append("对手的 ");
                }
                toAppendBuilder.append(attackerTrans).append("使用了");
                final String move = MyApplication.toId(split[1]);
                final String moveTrans=translateMove(split[1]);
                if (move.equals("batonpass")) {
                    battleFragment.setBatonPass(true);
                }
                toAppendBuilder.append(moveTrans).append("!");
                toAppend = toAppendBuilder.toString();
                start = toAppend.indexOf(moveTrans);
                toAppendSpannable = new SpannableString(toAppend);
                toAppendSpannable.setSpan(new StyleSpan(Typeface.BOLD),
                        start, start + moveTrans.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                logMessage = toAppendSpannable;
                toast = battleFragment.makeToast(logMessage);

                toast.addListener(new AnimatorListenerWithNet() {
                    @Override
                    public void onAnimationStartWithNet(Animator animation) {
                        if (battleFragment.getView() == null) {
                            return;
                        }

                        PokemonInfo pokemonInfo = battleFragment.getPokemonInfo(split[0]);
                        if (!move.equals("struggle")) {
                            HashMap<String, Integer> moves = pokemonInfo.getMoves();
                            if (moves.containsKey(move)) {
                                moves.put(move, moves.get(move) - 1);
                            } else {
                                moves.put(move, Integer.parseInt(MoveDex.getMoveMaxPP(battleFragment.getActivity(), move)) - 1);
                            }
                        }

                        if (!messageDetails.contains("[still]")) {
                            AnimatorSet animatorSet = BattleAnimation.processMove(move, battleFragment, split);
                            if (animatorSet != null && Onboarding.get(battleFragment.getActivity()).isAnimation()) {
                                battleFragment.setCurrentBattleAnimation(animatorSet);
                                animatorSet.addListener(new AnimatorListenerWithNet() {
                                    @Override
                                    public void onAnimationEndWithNet(Animator animation) {
                                        super.onAnimationEndWithNet(animation);
                                        battleFragment.setCurrentBattleAnimation(null);
                                    }
                                });
                                animatorSet.start();
                            }
                        }
                    }
                });
                battleFragment.startAnimation(toast, message);
                break;

            case "switch":
            case "drag":
            case "replace":
                final int toBeSwapped;

                //TODO need to handle roar & cie
                toAppendBuilder = new StringBuilder();

                int tempHp;
                String tempStatus;

                try {
                    tempHp = processHpFraction(split[2]);
                    tempStatus = processStatusFraction(split[2]);
                } catch (ArrayIndexOutOfBoundsException ex) { // zoroark's transform
                    tempHp = processHpFraction(split[0]);
                    tempStatus = processStatusFraction(split[0]);
                }

                final int hp = processHpFraction(split[2]);
                final String status = processStatusFraction(split[2]);

                String species = !split[1].contains(",") ? split[1] :
                        split[1].substring(0, split[1].indexOf(","));
                String speciesId = MyApplication.toId(species);
                pokemonInfo = new PokemonInfo(battleFragment.getActivity(), speciesId);
                processPokemonDetailString(pokemonInfo, split[1]);
                pokemonInfo.setHp(hp);
                pokemonInfo.setStatus(status);

                // Switching sprites and icons
                ArrayList<PokemonInfo> playerTeam = battleFragment.getTeam(split[0]);
                if (playerTeam == null) {
                    playerTeam = new ArrayList<>();
                }
                if (battleFragment.findPokemonInTeam(playerTeam, species) == -1) {
                    playerTeam.add(playerTeam.size(), pokemonInfo);
                    toBeSwapped = playerTeam.size() - 1;
                } else {
                    toBeSwapped = battleFragment.findPokemonInTeam(playerTeam, species);
                }
                int j = battleFragment.getTeamSlot(messageDetails);
                PokemonInfo holder = playerTeam.get(j);
                playerTeam.set(j, playerTeam.get(toBeSwapped));
                playerTeam.set(toBeSwapped, holder);

                battleFragment.setTeam(messageDetails, playerTeam);

                if (command.equals("switch")) {
                    if (messageDetails.startsWith("p2")) {
                        toAppendBuilder.append(battleFragment.getPlayer2()).append("放出了").append(translatePokemon(species)).append("!");
                    } else {
                        toAppendBuilder.append("去吧！ ").append(translatePokemon(species)).append("!");
                    }
                } else {
                    if (command.equals("drag")) {
                        toAppendBuilder.append(translatePokemon(species)).append("被拖入战斗！");
                    }
                }

                toast = battleFragment.makeToast(new SpannableStringBuilder(toAppendBuilder));
                toast.addListener(new AnimatorListenerWithNet() {
                    @Override
                    public void onAnimationStartWithNet(Animator animation) {
                        if (battleFragment.getView() == null) {
                            return;
                        }

                        battleFragment.displayPokemon(split[0]);

                        ImageView sprites = (ImageView) battleFragment.getView()
                                .findViewById(battleFragment.getSpriteId(split[0]));
                        if (sprites != null) {
                            sprites.setImageResource(pokemonInfo.getSprite(battleFragment.getActivity()));
                        }
                        ImageView iconLeader = (ImageView) battleFragment.getView()
                                .findViewById(battleFragment.getIconId(split[0], battleFragment.getTeamSlot(messageDetails)));
                        Drawable leader = iconLeader.getDrawable();
                        ImageView iconTrailer = (ImageView) battleFragment.getView()
                                .findViewById(battleFragment.getIconId(split[0], toBeSwapped));
                        float alphaTrailer = iconTrailer.getAlpha();
                        iconTrailer.setImageDrawable(leader);
                        iconTrailer.setAlpha(iconLeader.getAlpha());
                        iconLeader.setImageResource(pokemonInfo.getIcon(battleFragment.getActivity()));
                        iconLeader.setAlpha(alphaTrailer);

                        TextView pkmName = (TextView) battleFragment.getView()
                                .findViewById(battleFragment.getSpriteNameid(split[0]));
                        if (pkmName != null) {
                            if (pokemonInfo.getLevel() != 100) {
                                pkmName.setText(pokemonInfo.getNickname() + " L" + pokemonInfo.getLevel() + " ");
                            } else {
                                pkmName.setText(pokemonInfo.getNickname() + " ");
                            }
                        }

                        ImageView gender = (ImageView) battleFragment.getView()
                                .findViewById(battleFragment.getGenderId(split[0]));
                        gender.setImageResource(getGenderSprite(pokemonInfo.getGender()));

                        TextView hpText = (TextView) battleFragment.getView()
                                .findViewById(battleFragment.getHpId(messageDetails.substring(0, 3)));
                        ProgressBar hpBar = (ProgressBar) battleFragment.getView()
                                .findViewById(battleFragment.getHpBarId(messageDetails.substring(0, 3)));
                        if (hpText != null) {
                            hpText.setText(Integer.toString(hp));
                        }
                        if (hpBar != null) {
                            hpBar.setProgress(hp);
                        }

                        if (status != null) {
                            battleFragment.setAddonStatus(split[0], status.toLowerCase());
                        }
                    }
                });
                logMessage = new SpannableString(toAppendBuilder);
                battleFragment.startAnimation(toast, message);
                break;

            case "detailschange":
                final String forme = (split[1].contains(",")) ? split[1].substring(0, split[1].indexOf(',')) : split[1];

                position = split[0].substring(0, 3);

                battleFragment.formChange(position, forme);

                pokemonInfo = battleFragment.getPokemonInfo(position);

                toast = battleFragment.makeToast("形态变换", BattleFragment.ANIMATION_SHORT);
                toast.addListener(new AnimatorListenerWithNet() {
                    @Override
                    public void onAnimationStartWithNet(Animator animation) {
                        if (battleFragment.getView() == null) {
                            return;
                        }

                        boolean back = split[0].startsWith("p1");
                        ImageView sprite = (ImageView) battleFragment.getView().findViewById(battleFragment.getSpriteId(position));
                        sprite.setImageResource(Pokemon.getPokemonSprite(battleFragment.getActivity(),
                                MyApplication.toId(forme), back, pokemonInfo.isFemale(), pokemonInfo.isShiny()));
                        ImageView icon = (ImageView) battleFragment.getView().findViewById(battleFragment.getIconId(position));
                        icon.setImageResource(Pokemon.getPokemonIcon(battleFragment.getActivity(),
                                MyApplication.toId(forme)));
                    }
                });
                battleFragment.startAnimation(toast, message);
                break;

            case "faint":
                position = split[0];
                attacker = split[0].substring(5);
                toAppendBuilder = new StringBuilder();
                if (messageDetails.startsWith("p2")) {
                    toAppendBuilder.append("对手的");
                }
                toAppendBuilder.append(translatePokemon(attacker)).append("倒下了！");
                toast = battleFragment.makeToast(new SpannableStringBuilder(toAppendBuilder));
                toast.addListener(new AnimatorListenerWithNet() {
                    @Override
                    public void onAnimationStartWithNet(Animator animation) {
                        if (battleFragment.getView() == null) {
                            return;
                        }

                        PokemonInfo faintedPokemon = battleFragment.getPokemonInfo(split[0]);
                        faintedPokemon.setHp(0);
                        battleFragment.hidePokemon(position);
                        battleFragment.getView().findViewById(battleFragment.getIconId(position)).setAlpha(0.5f);
                    }
                });

                battleFragment.startAnimation(toast, message);
                logMessage = new SpannableString(toAppendBuilder);
                break;

            case "turn":
                if (battleFragment.getView() == null) {
                    return;
                }
                TextView turn = (TextView) battleFragment.getView().findViewById(R.id.turn);
                animator = ObjectAnimator.ofFloat(turn, "alpha", 0f, 1f);
                toAppend = "回合 " + messageDetails;
                animator.addListener(new AnimatorListenerWithNet() {
                    @Override
                    public void onAnimationStartWithNet(Animator animation) {
                        if (battleFragment.getView() == null) {
                            return;
                        }
                        battleFragment.getView().findViewById(R.id.turn).setVisibility(View.VISIBLE);
                        ((TextView) battleFragment.getView().findViewById(R.id.turn)).setText(toAppend);
                        (battleFragment.getView().findViewById(R.id.inactive)).setVisibility(View.GONE);
                    }
                });
                animator.setDuration(BattleFragment.ANIMATION_SHORT);
                animator.setInterpolator(new AccelerateDecelerateInterpolator());
                animatorSet = new AnimatorSet();
                animatorSet.play(animator);
                battleFragment.startAnimation(animatorSet, message);
                toAppendSpannable = new SpannableString(toAppend.toUpperCase());
                toAppendSpannable.setSpan(new UnderlineSpan(), 0, toAppend.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                toAppendSpannable.setSpan(new StyleSpan(Typeface.BOLD), 0, toAppend.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                toAppendSpannable.setSpan(new RelativeSizeSpan(1.25f), 0, toAppend.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                toAppendSpannable.setSpan(new ForegroundColorSpan(R.color.dark_blue), 0, toAppend.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                logMessage = new SpannableString(toAppendSpannable);
                break;

            case "win":
                toAppend = messageDetails + "获得了胜利！";
                toast = battleFragment.makeToast(new SpannableString(toAppend));
                battleFragment.startAnimation(toast, message);
                logMessage = new SpannableString(toAppend);

                if (battleFragment.getBattling() != 0) {
                    battleFragment.showEndBattleDialog(toAppend);
                }
                break;

            case "tie":
                toAppend = "这场战斗是平局！";
                toast = battleFragment.makeToast(new SpannableString(toAppend));
                battleFragment.startAnimation(toast, message);
                logMessage = new SpannableString(toAppend);
                if (battleFragment.getBattling() != 0) {
                    battleFragment.showEndBattleDialog(toAppend);
                }
                break;

            case "cant":
                String attackerOutputName = translatePokemon(battleFragment.getPrintableOutputPokemonSide(split[0]));
                toAppendBuilder = new StringBuilder();
                switch (MyApplication.toId(battleFragment.getPrintable(split[1]))) {
                    case "taunt":
                        toAppendBuilder.append(attackerOutputName).append("因为被挑拨而不能使用").append(translateMove(battleFragment.getPrintable(split[2]))).append("!");
                        break;

                    case "gravity":
                        toAppendBuilder.append(attackerOutputName).append("因为重力而不能使用").append(translateMove(battleFragment.getPrintable(split[2]))).append("!");
                        break;

                    case "healblock":
                        toAppendBuilder.append(attackerOutputName).append("因为回复封印而不能使用").append(translateMove(battleFragment.getPrintable(split[2]))).append("!");
                        break;

                    case "imprison":
                        toAppendBuilder.append(attackerOutputName).append("因为被封印而不能使用").append(translateMove(battleFragment.getPrintable(split[2]))).append("!");
                        break;

                    case "par":
                        toAppendBuilder.append(attackerOutputName).append("麻痹了！它不能行动！");
                        break;

                    case "frz":
                        toAppendBuilder.append(attackerOutputName).append("被结实地冰冻着。");
                        break;

                    case "slp":
                        toAppendBuilder.append(attackerOutputName).append("正在熟睡。");
                        break;

                    case "skydrop":
                        attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0], false);
                        toAppendBuilder.append("自由落体让").append(attackerOutputName).append("无法行动！");
                        break;

                    case "truant":
                        toAppendBuilder.append(attackerOutputName).append("正在闲逛！");
                        break;

                    case "recharge":
                        toAppendBuilder.append(attackerOutputName).append("必须充能！");
                        break;

                    case "focuspunch":
                        toAppendBuilder.append(attackerOutputName).append("失去了它的聚气并无法行动！");
                        break;

                    case "flinch":
                        toAppendBuilder.append(attackerOutputName).append("畏缩了并无法行动！");
                        break;

                    case "attract":
                        toAppendBuilder.append(attackerOutputName).append("陷入了爱河！");
                        break;

                    case "nopp":
                        toAppendBuilder.append(attackerOutputName).append("使用了").append(translateMove(battleFragment.getPrintable(split[2])));
                        toAppendBuilder.append("\n但技能已经没有PP了！");
                        break;

                    default:
                        toAppendBuilder.append(attackerOutputName);
                        if (split.length > 2) {
                            toAppendBuilder.append("不能使用").append(translateMove(battleFragment.getPrintable(split[2])));
                        } else {
                            toAppendBuilder.append("无法行动");
                        }
                        toAppendBuilder.append("!");
                        break;
                }
                logMessage = new SpannableString(toAppendBuilder);
                toast = battleFragment.makeMinorToast(logMessage);
                animatorSet = battleFragment.createFlyingMessage(split[0], toast, new SpannableString("Failed!"));
                battleFragment.startAnimation(animatorSet, message);
                break;

            default:
                /*toast = battleFragment.makeToast(message, BattleFragment.ANIMATION_LONG);
                battleFragment.startAnimation(toast, message);*/
                logMessage = new SpannableString(message);
                break;
        }

        battleFragment.addToLog(logMessage);
    }


    public static void processMinorActionZHCN(final BattleFragment battleFragment, String command, final String messageDetails, final String message) {
        int separator;
        final PokemonInfo pokemonInfo;
        Integer oldHP;
        final int lostHP;
        final int intAmount;
        String remaining;
        String toAppend;
        StringBuilder toAppendBuilder = new StringBuilder();
        Spannable toAppendSpannable;
        Spannable logMessage = new SpannableString("");
        String move, ability;
        boolean flag, eat, weaken;

        String fromEffect = null;
        String fromEffectId = null;
        String ofSource = null;
        String trimmedOfEffect = null;

        String attacker, defender, side, stat, statAmount;
        String attackerOutputName;
        String defenderOutputName;

        int from = messageDetails.indexOf("[from]");
        if (from != -1) {
            remaining = messageDetails.substring(from + 7);
            separator = remaining.indexOf('|');
            fromEffect = (separator == -1) ? remaining : remaining.substring(0, separator);
            //trim
            fromEffectId = MyApplication.toId(fromEffect);
        }
        int of = messageDetails.indexOf("[of]");
        if (of != -1) {
            remaining = messageDetails.substring(of + 5);
            separator = remaining.indexOf('|');
            ofSource = (separator == -1) ? remaining : remaining.substring(remaining.indexOf(':'), separator);

            trimmedOfEffect = MyApplication.toId(ofSource);
        }

        final String[] split = messageDetails.split("\\|");

        AnimatorSet toast;
        AnimatorSet animatorSet;

        if (battleFragment.getView() == null) {
            return;
        }

        switch (command) {
            case "-damage":
                attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0]);
                pokemonInfo = battleFragment.getPokemonInfo(messageDetails);
                oldHP = pokemonInfo.getHp();
                intAmount = processHpFraction(split[1]);
                pokemonInfo.setHp(intAmount);
                lostHP = intAmount - oldHP;

                if (fromEffectId != null) {
                    switch (battleFragment.trimOrigin(fromEffect)) {
                        case "stealthrock":
                            attackerOutputName = translatePokemon(battleFragment.getPrintableOutputPokemonSide(split[0], false));
                            toAppendBuilder.append("尖锐的岩石扎进了").append(attackerOutputName).append("的身体!");
                            break;
                        case "spikes":
                            toAppendBuilder.append(attackerOutputName).append("受到了撒菱的伤害！");
                            break;
                        case "brn":
                            toAppendBuilder.append(attackerOutputName).append("受到了灼伤的伤害！");
                            break;
                        case "psn":
                            toAppendBuilder.append(attackerOutputName).append("受到了中毒的伤害！");
                            break;
                        case "lifeorb":
                            toAppendBuilder.append(attackerOutputName).append("损失了一部分HP！");
                            pokemonInfo.setItem("lifeorb");
                            break;
                        case "recoil":
                            toAppendBuilder.append(attackerOutputName).append("受到了反作用力的伤害！");
                            break;
                        case "ironbarbs":
                            PokemonInfo barbTarget = battleFragment.getPokemonInfo(split[3]);
                            if (barbTarget != null) {
                                barbTarget.setAbility("ironbarbs");
                            }
                            toAppendBuilder.append(attackerOutputName).append("受到了").append(translatePokemon(battleFragment.getPrintable(ofSource))).append("的铁刺的伤害!");
                            break;
                        case "sandstorm":
                            toAppendBuilder.append(attackerOutputName).append("被沙暴打击了！");
                            break;
                        case "hail":
                            toAppendBuilder.append(attackerOutputName).append("被冰雹打击了！");
                            break;
                        case "baddreams":
                            toAppendBuilder.append(attackerOutputName).append("正在被折磨！");
                            break;
                        case "nightmare":
                            toAppendBuilder.append(attackerOutputName).append("被困在了恶梦之中！");
                            break;
                        case "confusion":
                            toAppendBuilder.append("它在混乱中攻击了自己！");
                            break;
                        case "leechseed":
                            toAppendBuilder.append(attackerOutputName).append("的生命值被寄生种子吸取了！");
                            break;
                        case "flameburst":
                            attackerOutputName = translatePokemon(battleFragment.getPrintableOutputPokemonSide(split[0], false));
                            toAppendBuilder.append("溅射的火焰击中了").append(attackerOutputName).append("!");
                            break;
                        case "firepledge":
                            toAppendBuilder.append(attackerOutputName).append("受到了火海的伤害！");
                            break;
                        case "jumpkick":
                        case "highjumpkick":
                            toAppendBuilder.append(attackerOutputName).append("摔了个狗啃泥！");
                            break;
                        case "rockyhelmet":
                            PokemonInfo helmetPokemon = battleFragment.getPokemonInfo(split[3]);
                            if (helmetPokemon != null) {
                                helmetPokemon.setItem("rockyhelmet");
                            }
                            toAppendBuilder.append(attackerOutputName).append("受到了来自").append(translatePokemon(battleFragment.getPrintable(ofSource))).append("凹凸头盔的伤害!");
                            break;
                        default:
                            if (ofSource != null) {
                                toAppendBuilder.append(attackerOutputName).append("受到了").append(translatePokemon(battleFragment.getPrintable(ofSource))).append("的 ").append(battleFragment.getPrintable(fromEffect)).append("的伤害!");
                            } else if (fromEffectId.contains(":")) {
                                toAppendBuilder.append(attackerOutputName).append("受到了它的").append(battleFragment.getPrintable(fromEffect)).append("的伤害!");
                            } else {
                                toAppendBuilder.append(attackerOutputName).append("失去了一部分生命值，因为").append(battleFragment.getPrintable(fromEffect)).append("!");
                            }
                            break;
                    }
                } else {
                    toAppendBuilder.append(attackerOutputName).append("失去了");
                    toAppendBuilder.append(-lostHP).append("% 的生命值！");
                }

                toast = battleFragment.makeMinorToast(new SpannableStringBuilder(toAppendBuilder));

                final TextView damage = new TextView(battleFragment.getActivity());
                damage.setText(lostHP + "%");
                damage.setBackgroundResource(R.drawable.editable_frame_light_red);
                damage.setPadding(2, 2, 2, 2);
                damage.setAlpha(0f);

                toast.addListener(new AnimatorListenerWithNet() {
                    @Override
                    public void onAnimationStartWithNet(Animator animation) {
                        if (battleFragment.getView() == null) {
                            return;
                        }
                        ((TextView) battleFragment.getView().findViewById(battleFragment.getHpId(messageDetails))).setText(Integer.toString(intAmount));

                        ImageView imageView = (ImageView) battleFragment.getView().findViewById(battleFragment.getSpriteId(messageDetails));

                        RelativeLayout relativeLayout = (RelativeLayout) battleFragment.getView().findViewById(battleFragment.getPkmLayoutId(messageDetails));
                        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                        layoutParams.addRule(RelativeLayout.ALIGN_TOP, battleFragment.getSpriteId(messageDetails));
                        layoutParams.addRule(RelativeLayout.ALIGN_LEFT, battleFragment.getSpriteId(messageDetails));
                        layoutParams.setMargins((int) (imageView.getWidth() * 0.5f), (int) (imageView.getHeight() * 0.5f), 0, 0);
                        relativeLayout.addView(damage, layoutParams);
                    }

                    @Override
                    public void onAnimationEndWithNet(Animator animation) {
                        if (battleFragment.getView() == null) {
                            return;
                        }

                        RelativeLayout relativeLayout = (RelativeLayout) battleFragment.getView().findViewById(battleFragment.getPkmLayoutId(messageDetails));
                        relativeLayout.removeView(damage);
                    }
                });

                ObjectAnimator fadeIn = ObjectAnimator.ofFloat(damage, "alpha", 0f, 1f);
                fadeIn.setInterpolator(new DecelerateInterpolator());
                fadeIn.setDuration(BattleFragment.ANIMATION_SHORT / 4);

                ObjectAnimator fadeOut = ObjectAnimator.ofFloat(damage, "alpha", 1f, 0f);
                fadeOut.setInterpolator(new AccelerateInterpolator());
                fadeOut.setStartDelay(BattleFragment.ANIMATION_SHORT / 2);
                fadeOut.setDuration(BattleFragment.ANIMATION_SHORT / 4);

                ProgressBar hpBar = (ProgressBar) battleFragment.getView().findViewById(battleFragment.getHpBarId(messageDetails));
                ObjectAnimator hpCountDownBar = ObjectAnimator.ofInt(hpBar, "progress", intAmount);
                hpCountDownBar.setDuration(BattleFragment.ANIMATION_SHORT);
                hpCountDownBar.setInterpolator(new AccelerateDecelerateInterpolator());

                animatorSet = new AnimatorSet();
                animatorSet.play(toast);
                animatorSet.play(hpCountDownBar).with(toast);
                animatorSet.play(fadeIn).with(toast);
                animatorSet.play(fadeOut).after(fadeIn);

                battleFragment.startAnimation(animatorSet, message);

                logMessage = new SpannableStringBuilder(toAppendBuilder);
                break;

            case "-heal":
                attackerOutputName = translatePokemon(battleFragment.getPrintableOutputPokemonSide(split[0]));
                pokemonInfo = battleFragment.getPokemonInfo(messageDetails);
                oldHP = pokemonInfo.getHp();
                intAmount = processHpFraction(split[1]);
                pokemonInfo.setHp(intAmount);
                lostHP = intAmount - oldHP;

                if (fromEffectId != null) {
                    switch (battleFragment.trimOrigin(fromEffect)) {
                        case "ingrain":
                            toAppendBuilder.append(attackerOutputName).append("从根中汲取了营养！");
                            break;
                        case "aquaring":
                            attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0], false);
                            toAppendBuilder.append("水流环回复了").append(attackerOutputName).append("的生命值！");
                            break;
                        case "raindish":
                        case "dryskin":
                        case "icebody":
                            toAppendBuilder.append(attackerOutputName).append("的").append(translateAbility(battleFragment.getPrintable(fromEffect))).append("治疗了它！");
                            if (pokemonInfo != null) {
                                pokemonInfo.setAbility(battleFragment.getPrintable(fromEffect));
                            }
                            break;
                        case "healingwish":
                            attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0], false);
                            toAppendBuilder.append("治愈之愿降临在了").append(attackerOutputName).append("!");
                            break;
                        case "lunardance":
                            toAppendBuilder.append(attackerOutputName).append("被神秘的月光笼罩！");
                            break;
                        case "wish":
                            //TODO TRY
                            String wisher;
                            if (messageDetails.contains("[wisher]")) {
                                separator = messageDetails.substring(messageDetails.indexOf("[wisher]")).indexOf("|");
                                if (separator != -1) {
                                    wisher = messageDetails.substring(messageDetails.indexOf("[wisher]") + 8, separator);
                                } else {
                                    wisher = messageDetails.substring(messageDetails.indexOf("[wisher]") + 8);
                                }
                                toAppendBuilder.append(translatePokemon(battleFragment.getPrintableOutputPokemonSide(wisher))).append("的祈愿成真了！");
                            }
                            break;
                        case "drain":
                            if (trimmedOfEffect != null) {
                                toAppendBuilder.append(battleFragment.getPrintableOutputPokemonSide(ofSource)).append("吸取了能量！");
                                break;
                            }
                            // we should never enter here
                            toAppendBuilder.append(attackerOutputName).append(" drained health!");
                            break;

                        case "leftovers":
                        case "shellbell":
                        case "blacksludge":
                            toAppendBuilder.append(attackerOutputName).append("通过").append(translateAbility(battleFragment.getPrintable(fromEffect))).append("回复了一点HP!");
                            break;
                        default:
                            toAppendBuilder.append(attackerOutputName).append("通过").append(translateAbility(battleFragment.getPrintable(fromEffect))).append("回复了一点HP!");
                            break;
                    }
                } else {
                    toAppendBuilder.append(attackerOutputName);
                    toAppendBuilder.append("回复了").append(lostHP).append("%的生命值！");
                }

                toast = battleFragment.makeMinorToast(new SpannableStringBuilder(toAppendBuilder));

                final TextView heal = new TextView(battleFragment.getActivity());
                heal.setText(lostHP + "%");
                heal.setBackgroundResource(R.drawable.editable_frame_light_green);
                heal.setPadding(2, 2, 2, 2);
                heal.setAlpha(0f);

                toast.addListener(new AnimatorListenerWithNet() {
                    @Override
                    public void onAnimationStartWithNet(Animator animation) {
                        if (battleFragment.getView() == null) {
                            return;
                        }
                        ((TextView) battleFragment.getView().findViewById(battleFragment.getHpId(messageDetails))).setText(Integer.toString(intAmount));

                        ImageView imageView = (ImageView) battleFragment.getView().findViewById(battleFragment.getSpriteId(messageDetails));

                        RelativeLayout relativeLayout = (RelativeLayout) battleFragment.getView().findViewById(battleFragment.getPkmLayoutId(messageDetails));
                        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                        layoutParams.addRule(RelativeLayout.ALIGN_TOP, battleFragment.getSpriteId(messageDetails));
                        layoutParams.addRule(RelativeLayout.ALIGN_LEFT, battleFragment.getSpriteId(messageDetails));
                        layoutParams.setMargins((int) (imageView.getWidth() * 0.5f), (int) (imageView.getHeight() * 0.5f), 0, 0);
                        relativeLayout.addView(heal, layoutParams);
                    }

                    @Override
                    public void onAnimationEndWithNet(Animator animation) {
                        if (battleFragment.getView() == null) {
                            return;
                        }

                        RelativeLayout relativeLayout = (RelativeLayout) battleFragment.getView().findViewById(battleFragment.getPkmLayoutId(messageDetails));
                        relativeLayout.removeView(heal);
                    }
                });

                fadeIn = ObjectAnimator.ofFloat(heal, "alpha", 0f, 1f);
                fadeIn.setInterpolator(new DecelerateInterpolator());
                fadeIn.setDuration(BattleFragment.ANIMATION_SHORT / 4);

                fadeOut = ObjectAnimator.ofFloat(heal, "alpha", 1f, 0f);
                fadeOut.setInterpolator(new AccelerateInterpolator());
                fadeOut.setStartDelay(BattleFragment.ANIMATION_SHORT / 2);
                fadeOut.setDuration(BattleFragment.ANIMATION_SHORT / 4);

                hpBar = (ProgressBar) battleFragment.getView().findViewById(battleFragment.getHpBarId(messageDetails));
                hpCountDownBar = ObjectAnimator.ofInt(hpBar, "progress", intAmount);
                hpCountDownBar.setDuration(BattleFragment.ANIMATION_SHORT);
                hpCountDownBar.setInterpolator(new AccelerateDecelerateInterpolator());

                animatorSet = new AnimatorSet();
                animatorSet.play(toast);
                animatorSet.play(hpCountDownBar).with(toast);
                animatorSet.play(fadeIn).with(toast);
                animatorSet.play(fadeOut).after(fadeIn);

                battleFragment.startAnimation(animatorSet, message);

                logMessage = new SpannableStringBuilder(toAppendBuilder);
                break;
            case "-sethp":
                switch (battleFragment.trimOrigin(fromEffect)) {
                    case "painsplit":
                        toAppendBuilder.append("战斗者分担了他们的痛楚！");
                        toast = battleFragment.makeMinorToast(new SpannableString(toAppendBuilder));

                        PokemonInfo pkmA = battleFragment.getPokemonInfo(split[0]);
                        final int pkmAHp = processHpFraction(split[1]);
                        pkmA.setHp(pkmAHp);
                        PokemonInfo pkmB = battleFragment.getPokemonInfo(split[2]);
                        final int pkmBHp = processHpFraction(split[3]);
                        pkmB.setHp(pkmBHp);

                        toast.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                if (battleFragment.getView() == null) {
                                    return;
                                }

                                ((TextView) battleFragment.getView().findViewById(battleFragment.getHpId(split[0])))
                                        .setText(Integer.toString(pkmAHp));
                                ((TextView) battleFragment.getView().findViewById(battleFragment.getHpId(split[2])))
                                        .setText(Integer.toString(pkmBHp));
                            }
                        });

                        ProgressBar pkmAHpBar = (ProgressBar) battleFragment.getView().findViewById(battleFragment.getHpBarId(split[0]));
                        ObjectAnimator pkmACountDown = ObjectAnimator.ofInt(pkmAHpBar, "progress", pkmAHp);
                        pkmACountDown.setDuration(BattleFragment.ANIMATION_SHORT);
                        pkmACountDown.setInterpolator(new AccelerateDecelerateInterpolator());

                        ProgressBar pkmBHpBar = (ProgressBar) battleFragment.getView().findViewById(battleFragment.getHpBarId(split[2]));
                        ObjectAnimator pkmBCountDown = ObjectAnimator.ofInt(pkmBHpBar, "progress", pkmBHp);
                        pkmBCountDown.setDuration(BattleFragment.ANIMATION_SHORT);
                        pkmBCountDown.setInterpolator(new AccelerateDecelerateInterpolator());

                        animatorSet = new AnimatorSet();
                        animatorSet.play(toast);
                        animatorSet.play(pkmACountDown).with(toast);
                        animatorSet.play(pkmBCountDown).with(toast);

                        battleFragment.startAnimation(animatorSet, message);
                        break;
                }
                logMessage = new SpannableStringBuilder(toAppendBuilder);
                break;

            case "-boost":
                attackerOutputName = translatePokemon(battleFragment.getPrintableOutputPokemonSide(split[0]));
                stat = split[1];
                final String increasedStat;
                increasedStat = stat;
                statAmount = "";
                switch (stat) {
                    case "atk":
                        stat = "物攻";
                        break;
                    case "def":
                        stat = "物防";
                        break;
                    case "spa":
                        stat = "特攻";
                        break;
                    case "spd":
                        stat = "特防";
                        break;
                    case "spe":
                        stat = "速度";
                        break;
                    default:
                        break;
                }
                String amount = split[2];
                intAmount = Integer.parseInt(amount);
                if (intAmount == 2) {
                    statAmount = "大幅";
                } else if (intAmount > 2) {
                    statAmount = "巨幅";
                }

                if (fromEffect != null) {
                    if (fromEffect.contains("item:")) {
                        attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0], false);
                        toAppendBuilder.append(translateAbility(battleFragment.getPrintable(fromEffect))).append(statAmount).append("提升了").append(attackerOutputName).append("的").append(stat).append("!");
                    } else {
                        toAppendBuilder.append(attackerOutputName).append("的").append(translateAbility(battleFragment.getPrintable(fromEffect))).append(statAmount).append("提升了它的").append(stat).append("!");
                    }
                } else {
                    toAppendBuilder.append(attackerOutputName).append("的").append(stat).append(statAmount).append("提升了！");
                }

                toast = battleFragment.makeMinorToast(new SpannableStringBuilder(toAppendBuilder));

                toast.addListener(new AnimatorListenerWithNet() {
                    @Override
                    public void onAnimationStartWithNet(Animator animation) {
                        battleFragment.processBoost(messageDetails, increasedStat, intAmount);
                    }
                });
                battleFragment.startAnimation(toast, message);
                logMessage = new SpannableStringBuilder(toAppendBuilder);
                break;

            case "-unboost":
                attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0]);
                stat = split[1];
                increasedStat = stat;
                statAmount = "";

                switch (stat) {
                    case "atk":
                        stat = "物攻";
                        break;
                    case "def":
                        stat = "物防";
                        break;
                    case "spa":
                        stat = "特攻";
                        break;
                    case "spd":
                        stat = "特防";
                        break;
                    case "spe":
                        stat = "速度";
                        break;
                    default:
                        break;
                }
                amount = split[2];
                intAmount = -1 * Integer.parseInt(amount);
                if (intAmount == -2) {
                    statAmount = " 大幅";
                } else if (intAmount <= -3) {
                    statAmount = " 巨幅";
                }

                if (fromEffect != null) {
                    if (fromEffect.contains("item:")) {
                        attackerOutputName = translatePokemon(battleFragment.getPrintableOutputPokemonSide(split[0], false));
                        toAppendBuilder.append(translateAbility(battleFragment.getPrintable(fromEffect))).append(statAmount).append("降低了").append(attackerOutputName).append("的").append(stat).append("!");
                    } else {
                        toAppendBuilder.append(attackerOutputName).append("的").append(translateAbility(battleFragment.getPrintable(fromEffect))).append(statAmount).append("降低了它的").append(stat).append("!");
                    }
                } else {
                    toAppendBuilder.append(attackerOutputName).append("的").append(stat).append(statAmount).append("降低了！");
                }

                toast = battleFragment.makeMinorToast(new SpannableStringBuilder(toAppendBuilder));
                toast.addListener(new AnimatorListenerWithNet() {
                    @Override
                    public void onAnimationStartWithNet(Animator animation) {
                        battleFragment.processBoost(messageDetails, increasedStat, intAmount);
                    }
                });
                battleFragment.startAnimation(toast, message);
                logMessage = new SpannableStringBuilder(toAppendBuilder);
                break;

            case "-setboost":
                attackerOutputName = translatePokemon(battleFragment.getPrintableOutputPokemonSide(split[0]));
                if (fromEffect != null) {
                    switch (battleFragment.trimOrigin(fromEffect)) {
                        case "bellydrum":
                            toAppendBuilder.append(attackerOutputName).append("舍弃了自己的HP并最大化了它的攻击！");
                            toast = battleFragment.makeMinorToast(new SpannableString(toAppendBuilder));
                            toast.addListener(new AnimatorListenerWithNet() {
                                @Override
                                public void onAnimationStartWithNet(Animator animation) {
                                    battleFragment.processBoost(split[0], "atk", 6);
                                }
                            });
                            battleFragment.startAnimation(toast, message);
                            break;

                        case "angerpoint":
                            toAppendBuilder.append(attackerOutputName).append("最大化了它的攻击!");
                            toast = battleFragment.makeMinorToast(new SpannableString(toAppendBuilder));
                            toast.addListener(new AnimatorListenerWithNet() {
                                @Override
                                public void onAnimationStartWithNet(Animator animation) {
                                    battleFragment.processBoost(split[0], "atk", 6);
                                    battleFragment.getPokemonInfo(split[0]).setAbility("angerpoint");
                                }
                            });
                            battleFragment.startAnimation(toast, message);
                            break;
                    }
                }
                logMessage = new SpannableStringBuilder(toAppendBuilder);
                break;

            case "-swapboost":
                attackerOutputName = translatePokemon(battleFragment.getPrintableOutputPokemonSide(split[0]));
                if (fromEffect != null) {
                    switch (battleFragment.trimOrigin(fromEffect)) {
                        case "guardswap":
                            toAppendBuilder.append(attackerOutputName).append("交换了它和目标间的所有防御和特防变化！");
                            toast = battleFragment.makeMinorToast(new SpannableStringBuilder(toAppendBuilder));
                            toast.addListener(new AnimatorListenerWithNet() {
                                @Override
                                public void onAnimationStartWithNet(Animator animation) {
                                    battleFragment.swapBoost(split[0], split[1], "def", "spd");
                                }
                            });
                            battleFragment.startAnimation(toast, message);
                            break;

                        case "heartswap":
                            toAppendBuilder.append(attackerOutputName).append("交换了它和目标间的能力变化！");
                            toast = battleFragment.makeMinorToast(new SpannableStringBuilder(toAppendBuilder));
                            toast.addListener(new AnimatorListenerWithNet() {
                                @Override
                                public void onAnimationStartWithNet(Animator animation) {
                                    battleFragment.swapBoost(split[0], split[1], BattleFragment.STATS);
                                }
                            });
                            battleFragment.startAnimation(toast, message);
                            break;

                        case "powerswap":
                            toAppendBuilder.append(attackerOutputName).append("交换了它和目标间的所有攻击和特攻变化！");
                            toast = battleFragment.makeMinorToast(new SpannableStringBuilder(toAppendBuilder));
                            toast.addListener(new AnimatorListenerWithNet() {
                                @Override
                                public void onAnimationStartWithNet(Animator animation) {
                                    battleFragment.swapBoost(split[0], split[1], "atk", "spa");
                                }
                            });
                            battleFragment.startAnimation(toast, message);
                            break;
                    }
                    logMessage = new SpannableStringBuilder(toAppendBuilder);
                }
                break;

            case "-restoreboost":
                attackerOutputName = translatePokemon(battleFragment.getPrintableOutputPokemonSide(split[0]));
                toAppendBuilder.append(attackerOutputName).append("移除了不利的能力变化！");
                toast = battleFragment.makeMinorToast(new SpannableStringBuilder(toAppendBuilder));
                toast.addListener(new AnimatorListenerWithNet() {
                    @Override
                    public void onAnimationStartWithNet(Animator animation) {
                        if (battleFragment.getView() == null) {
                            return;
                        }
                        battleFragment.restoreBoost(split[0]);
                    }
                });
                battleFragment.startAnimation(toast, message);
                logMessage = new SpannableStringBuilder(toAppendBuilder);
                break;

            case "-clearnegativeboost":
                attackerOutputName = translatePokemon(battleFragment.getPrintableOutputPokemonSide(split[0]));
                toAppendBuilder.append(attackerOutputName).append("移除了不利的能力变化！");
                toast = battleFragment.makeMinorToast(new SpannableStringBuilder(toAppendBuilder));
                toast.addListener(new AnimatorListenerWithNet() {
                    @Override
                    public void onAnimationStartWithNet(Animator animation) {
                        if (battleFragment.getView() == null) {
                            return;
                        }
                        battleFragment.clearnegativeBoost(split[0]);
                    }
                });
                battleFragment.startAnimation(toast, message);
                logMessage = new SpannableStringBuilder(toAppendBuilder);
                break;

            case "-clearpositiveboost":
                attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0]);
                toAppendBuilder.append(attackerOutputName).append("移除了有利的能力变化！");
                toast = battleFragment.makeMinorToast(new SpannableStringBuilder(toAppendBuilder));
                toast.addListener(new AnimatorListenerWithNet() {
                    @Override
                    public void onAnimationStartWithNet(Animator animation) {
                        if (battleFragment.getView() == null) {
                            return;
                        }
                        battleFragment.clearpositiveBoost(split[0]);
                    }
                });
                battleFragment.startAnimation(toast, message);
                logMessage = new SpannableStringBuilder(toAppendBuilder);
                break;

            case "-copyboost":
                attackerOutputName = translatePokemon(battleFragment.getPrintableOutputPokemonSide(split[0]));
                defenderOutputName = translatePokemon(battleFragment.getPrintableOutputPokemonSide(split[1], false));
                toAppendBuilder.append(attackerOutputName).append("复制了").append(defenderOutputName).append("的能力变化！");
                toast = battleFragment.makeMinorToast(new SpannableStringBuilder(toAppendBuilder));
                toast.addListener(new AnimatorListenerWithNet() {
                    @Override
                    public void onAnimationStartWithNet(Animator animation) {
                        if (battleFragment.getView() == null) {
                            return;
                        }
                        LinearLayout linearLayout = (LinearLayout) battleFragment.getView().findViewById(battleFragment.getTempStatusId(split[0]));
                        for (String stat : BattleFragment.STATS) {
                            TextView v = (TextView) linearLayout.findViewWithTag(stat);
                            linearLayout.removeView(v);
                        }
                        battleFragment.copyBoost(split[1], split[0]);
                    }
                });
                battleFragment.startAnimation(toast, message);
                logMessage = new SpannableStringBuilder(toAppendBuilder);
                break;

            case "-clearboost":
                attackerOutputName = translatePokemon(battleFragment.getPrintableOutputPokemonSide(split[0]));
                toAppendBuilder.append(attackerOutputName).append("的能力变化被重置了！");
                toast = battleFragment.makeMinorToast(new SpannableStringBuilder(toAppendBuilder));
                toast.addListener(new AnimatorListenerWithNet() {
                    @Override
                    public void onAnimationStartWithNet(Animator animation) {
                        if (battleFragment.getView() == null) {
                            return;
                        }
                        LinearLayout linearLayout = (LinearLayout) battleFragment.getView().findViewById(battleFragment.getTempStatusId(split[0]));
                        for (String stat : BattleFragment.STATS) {
                            TextView v = (TextView) linearLayout.findViewWithTag(stat);
                            linearLayout.removeView(v);
                        }
                    }
                });
                battleFragment.startAnimation(toast, message);
                logMessage = new SpannableStringBuilder(toAppendBuilder);
                break;

            case "-invertboost":
                attackerOutputName = translatePokemon(battleFragment.getPrintableOutputPokemonSide(split[0]));
                toAppendBuilder.append(attackerOutputName).append("的能力阶级变化被反转了！");
                toast = battleFragment.makeMinorToast(new SpannableStringBuilder(toAppendBuilder));
                toast.addListener(new AnimatorListenerWithNet() {
                    @Override
                    public void onAnimationStartWithNet(Animator animation) {
                        battleFragment.invertBoost(split[0], BattleFragment.STATS);
                    }
                });
                battleFragment.startAnimation(toast, message);
                logMessage = new SpannableStringBuilder(toAppendBuilder);
                break;

            case "-clearallboost":
                toAppendBuilder.append("所有能力阶级变化都被消除了!");
                toast = battleFragment.makeMinorToast(new SpannableStringBuilder(toAppendBuilder));
                toast.addListener(new AnimatorListenerWithNet() {
                    @Override
                    public void onAnimationStartWithNet(Animator animation) {
                        if (battleFragment.getView() == null) {
                            return;
                        }
                        String[] layouts = {"p1a", "p1b", "p1c", "p2a", "p2b", "p2c"};
                        for (String layout : layouts) {
                            LinearLayout linearLayout = (LinearLayout) battleFragment.getView().findViewById(battleFragment.getTempStatusId(layout));
                            for (String stat : BattleFragment.STATS) {
                                TextView v = (TextView) linearLayout.findViewWithTag(stat);
                                linearLayout.removeView(v);
                            }
                        }
                    }
                });
                battleFragment.startAnimation(toast, message);
                logMessage = new SpannableStringBuilder(toAppendBuilder);
                break;

            case "-crit":
                toAppendSpannable = new SpannableString("击中了要害！");
                toast = battleFragment.makeMinorToast(toAppendSpannable);
                animatorSet = battleFragment.createFlyingMessage(split[0], toast, new SpannableString("Critical!"));
                battleFragment.startAnimation(animatorSet, message);
                logMessage = new SpannableStringBuilder(toAppendSpannable);
                break;

            case "-supereffective":
                toAppendSpannable = new SpannableString("这非常有效！");
                toast = battleFragment.makeMinorToast(toAppendSpannable);
                animatorSet = battleFragment.createFlyingMessage(split[0], toast, new SpannableString("Super Effective!"));
                battleFragment.startAnimation(animatorSet, message);
                logMessage = new SpannableStringBuilder(toAppendSpannable);
                break;

            case "-resisted":
                toAppendSpannable = new SpannableString("这不是很有效...");
                toast = battleFragment.makeMinorToast(toAppendSpannable);
                animatorSet = battleFragment.createFlyingMessage(split[0], toast, new SpannableString("Resisted!"));
                battleFragment.startAnimation(animatorSet, message);
                logMessage = new SpannableStringBuilder(toAppendSpannable);
                break;

            case "-immune":
                attackerOutputName = translatePokemon(battleFragment.getPrintableOutputPokemonSide(split[0], false));
                toAppendBuilder.append("这对");
                toAppendBuilder.append(attackerOutputName);
                toAppendBuilder.append("没有效果！");
                toAppendSpannable = new SpannableString(toAppendBuilder);
                toast = battleFragment.makeMinorToast(toAppendSpannable);
                animatorSet = battleFragment.createFlyingMessage(split[0], toast, new SpannableString("Immuned!"));
                battleFragment.startAnimation(animatorSet, message);
                logMessage = new SpannableStringBuilder(toAppendBuilder);
                break;

            case "-miss":
                if (split.length > 1) {
                    // there was a target
                    defenderOutputName = translatePokemon(battleFragment.getPrintableOutputPokemonSide(split[1]));
                    toAppendBuilder.append(defenderOutputName).append("回避了攻击!");
                } else {
                    attackerOutputName = translatePokemon(battleFragment.getPrintableOutputPokemonSide(split[0]));
                    toAppendBuilder.append(attackerOutputName).append("的攻击没有命中！");
                }
                toAppendSpannable = new SpannableStringBuilder(toAppendBuilder);
                toast = battleFragment.makeMinorToast(toAppendSpannable);
                animatorSet = battleFragment.createFlyingMessage(split[0], toast, new SpannableString("Missed!"));
                battleFragment.startAnimation(animatorSet, message);
                logMessage = new SpannableStringBuilder(toAppendBuilder);
                break;

            case "-fail":
                attackerOutputName = translatePokemon(battleFragment.getPrintableOutputPokemonSide(split[0]));
                if (split.length > 1) {
                    remaining = split[1];

                    switch (remaining) {
                        case "brn":
                            toAppendBuilder.append(attackerOutputName).append("已经灼伤了.");
                            break;
                        case "tox":
                        case "psn":
                            toAppendBuilder.append(attackerOutputName).append("已经中毒了.");
                            break;
                        case "slp":
                            if (fromEffect != null && battleFragment.getPrintable(fromEffectId).equals("uproar")) {
                                attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0], false);
                                toAppendBuilder.append("但是吵闹使得").append(attackerOutputName).append("清醒了！");
                            } else {
                                toAppendBuilder.append(attackerOutputName).append("已经睡着了.");
                            }
                            break;
                        case "par":
                            toAppendBuilder.append(attackerOutputName).append("已经被麻痹了！");
                            break;
                        case "frz":
                            toAppendBuilder.append(attackerOutputName).append("已经被冻住了！");
                            break;
                        case "hyperspacefury":
                            toAppendBuilder.append(attackerOutputName).append("不能使用这个技能！");
                            break;
                        case "magikarpsrevenge":
                            toAppendBuilder.append("但是").append(attackerOutputName).append(" 不能使用这个技能！");
                            break;
                        case "substitute":
                            if (messageDetails.contains("[weak]")) {
                                toAppendBuilder.append(attackerOutputName).append("然而它并没有足够的HP来制造一个替身！");
                            } else {
                                toAppendBuilder.append(attackerOutputName).append("已经有一个替身了！");
                            }
                            break;
                        case "skydrop":
                            if (messageDetails.contains("[heavy]")) {
                                toAppendBuilder.append(attackerOutputName).append("太重了无法被提起！");
                            } else {
                                toAppendBuilder.append("但是失败了!");
                            }
                            break;
                        case "sunnyday":
                        case "raindance":
                        case "sandstorm":
                        case "hail":
                            switch (battleFragment.trimOrigin(fromEffect)) {
                                case "desolateland":
                                    toAppendBuilder.append("强烈的阳光丝毫没有被削弱！");
                                    break;
                                case "primordialsea":
                                    toAppendBuilder.append("这场大雨仍然在倾泻着!");
                                    break;
                                case "deltastream":
                                    toAppendBuilder.append("神秘的气流仍然在持续着!");
                                    break;
                                default:
                                    toAppendBuilder.append("但是失败了!");
                            }
                            break;
                        case "unboost":
                            toAppendBuilder.append(attackerOutputName).append("的能力阶级没有被降低！");
                            break;

                        default:
                            if (fromEffect != null) {
                                switch (battleFragment.trimOrigin(fromEffect)) {
                                    case "desolateland":
                                        toAppendBuilder.append("水属性攻击在强烈的阳光中蒸发殆尽!");
                                        break;
                                    case "primordialsea":
                                        toAppendBuilder.append("火属性攻击在大雨中消耗殆尽！");
                                        break;
                                    default:
                                        toAppendBuilder.append("但是失败了!");
                                }
                            } else {
                                toAppendBuilder.append("但是失败了!");
                            }
                            break;
                    }
                } else {
                    toAppendBuilder.append("但是失败了!");
                }
                toast = battleFragment.makeMinorToast(new SpannableString(toAppendBuilder));
                animatorSet = battleFragment.createFlyingMessage(split[0], toast, new SpannableString("但是失败了!"));
                battleFragment.startAnimation(animatorSet, message);

                logMessage = new SpannableString(toAppendBuilder);
                break;

            case "-notarget":
                logMessage = new SpannableString("然而攻击没有目标...");
                toast = battleFragment.makeMinorToast(logMessage);
                battleFragment.startAnimation(toast, message);
                break;

            case "-ohko":
                logMessage = new SpannableString("这是一击必杀!");
                toast = battleFragment.makeMinorToast(logMessage);
                battleFragment.startAnimation(toast, message);
                break;

            case "-hitcount":
                try {
                    String hitCountS = split[split.length - 1];
                    int hitCount = Integer.parseInt(hitCountS);
                    toAppendBuilder.append("击中了").append(hitCount).append("次！");
                    logMessage = new SpannableStringBuilder(toAppendBuilder);
                } catch (NumberFormatException e) {
                    logMessage = new SpannableString(command + ":" + messageDetails);
                }
                toast = battleFragment.makeMinorToast(logMessage);
                battleFragment.startAnimation(toast, message);
                break;

            case "-nothing":
                logMessage = new SpannableString("但是什么都没有发生！ ");
                toast = battleFragment.makeMinorToast(logMessage);
                battleFragment.startAnimation(toast, message);
                break;

            case "-waiting":
                attackerOutputName = translatePokemon(battleFragment.getPrintableOutputPokemonSide(split[0]));
                defenderOutputName = translatePokemon(battleFragment.getPrintableOutputPokemonSide(split[1], false));
                toAppendBuilder.append(attackerOutputName).append("在等待着").append(defenderOutputName).append("的行动...");
                logMessage = new SpannableString(toAppendBuilder);
                toast = battleFragment.makeMinorToast(logMessage);
                battleFragment.startAnimation(toast, message);
                break;

            case "-combine":
                logMessage = new SpannableString("这两个招式合并为了一个招式!这是一个合体招式!");
                toast = battleFragment.makeMinorToast(logMessage);
                battleFragment.startAnimation(toast, message);
                break;

            case "-prepare":
                attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0]);
                logMessage = new SpannableString(attackerOutputName + "正在吸取力量!");
                toast = battleFragment.makeMinorToast(logMessage);
                battleFragment.startAnimation(toast, message);
                break;

            case "-status":
                attackerOutputName = translatePokemon(battleFragment.getPrintableOutputPokemonSide(split[0]));
                toAppendBuilder.append(attackerOutputName);
                remaining = split[1];
                switch (remaining) {
                    case "brn":
                        toAppendBuilder.append("被");
                        if (fromEffect != null) {
                            toAppendBuilder.append(translateAbility(battleFragment.getPrintable(fromEffect)));
                        }
                        toAppendBuilder.append("灼伤了!");
                        break;

                    case "tox":
                        if (fromEffect != null) {
                            toAppendBuilder.append("因为").append(translateAbility(battleFragment.getPrintable(fromEffect)));
                        }
                        toAppendBuilder.append("中了剧毒!");
                        break;

                    case "psn":
                        toAppendBuilder.append("中毒了！");
                        break;

                    case "slp":
                        if (fromEffect != null && fromEffectId.equals("moverest")) {
                            toAppendBuilder.append("睡着了并恢复了健康!");
                        } else {
                            toAppendBuilder.append("睡着了!");
                        }
                        break;

                    case "par":
                        toAppendBuilder.append("被麻痹了!有可能无法行动!");
                        break;

                    case "frz":
                        toAppendBuilder.append("被冻住了!");
                        break;
                }
                logMessage = new SpannableStringBuilder(toAppendBuilder);
                toast = battleFragment.makeMinorToast(logMessage);
                final String status;
                status = remaining;
                final boolean rest = (fromEffectId != null && fromEffectId.equals("moverest"));
                toast.addListener(new AnimatorListenerWithNet() {
                    @Override
                    public void onAnimationStartWithNet(Animator animation) {
                        battleFragment.setAddonStatus(split[0], status);
                        if (rest) {
                            for (String stt : BattleFragment.STTUS) {
                                if (!stt.equals("slp")) {
                                    battleFragment.removeAddonStatus(split[0], stt);
                                }
                            }
                        }
                    }
                });
                battleFragment.startAnimation(toast, message);
                break;

            case "-curestatus":
                attackerOutputName = translatePokemon(battleFragment.getPrintableOutputPokemonSide(split[0]));
                flag = false;
                if (fromEffectId != null) {
                    fromEffectId = battleFragment.getPrintable(fromEffectId);
                    switch (battleFragment.trimOrigin(fromEffect)) {
                        case "psychoshift":
                            defenderOutputName = translatePokemon(battleFragment.getPrintableOutputPokemonSide(ofSource, false));
                            toAppendBuilder.append(attackerOutputName).append("将它的异常状态转移给了").append(defenderOutputName);
                            flag = true;
                            break;
                    }
                    if (fromEffectId.contains("ability:")) {
                        toAppendBuilder.append(attackerOutputName).append("'s ").append(translateAbility(battleFragment.getPrintable(fromEffect))).append("治愈了它的异常状态!");
                        flag = true;
                    }
                }

                if (!flag) {
                    //split1 is cured status
                    switch (split[1]) {
                        case "brn":
                            if (fromEffectId != null && fromEffectId.contains("item:")) {
                                toAppendBuilder.append(attackerOutputName).append("的 ").append(translateAbility(battleFragment.getPrintable(fromEffect))).append("治愈了它的灼伤状态!");
                                break;
                            }
                            if (split[0].startsWith("p2")) {
                                toAppendBuilder.append(attackerOutputName).append("的灼伤被治愈了。");
                            } else {
                                toAppendBuilder.append(attackerOutputName).append(" 治愈了它的灼伤状态!");
                            }
                            break;

                        case "tox":
                        case "psn":
                            if (fromEffectId != null && fromEffectId.contains("item:")) {
                                toAppendBuilder.append(attackerOutputName).append("的 ").append(translateAbility(battleFragment.getPrintable(fromEffect))).append("治愈了它的中毒状态!");
                                break;
                            }
                            toAppendBuilder.append(attackerOutputName).append("的中毒状态被治愈了！");
                            break;

                        case "slp":
                            if (fromEffectId != null && fromEffectId.contains("item:")) {
                                toAppendBuilder.append(attackerOutputName).append("的 ").append(translateAbility(battleFragment.getPrintable(fromEffect))).append("让它醒来了！");
                                break;
                            }
                            toAppendBuilder.append(attackerOutputName).append("醒来了！");
                            break;

                        case "par":
                            if (fromEffectId != null && fromEffectId.contains("item:")) {
                                toAppendBuilder.append(attackerOutputName).append("的 ").append(translateAbility(battleFragment.getPrintable(fromEffect))).append("治愈了它的麻痹状态!");
                                break;
                            }
                            toAppendBuilder.append(attackerOutputName).append("的麻痹状态被解除了.");

                            break;

                        case "frz":
                            if (fromEffectId != null && fromEffectId.contains("item:")) {
                                toAppendBuilder.append(attackerOutputName).append("的 ").append(translateAbility(battleFragment.getPrintable(fromEffect))).append(" 让它解冻了！");
                                break;
                            }
                            toAppendBuilder.append(attackerOutputName).append("解冻了!");
                            break;

                        default:
                            //confusion
                            toAppendBuilder.append(attackerOutputName).append("的混乱状态解除了!");
                            break;
                    }
                }
                logMessage = new SpannableStringBuilder(toAppendBuilder);
                toast = battleFragment.makeMinorToast(logMessage);
                toast.addListener(new AnimatorListenerWithNet() {
                    @Override
                    public void onAnimationStartWithNet(Animator animation) {
                        battleFragment.removeAddonStatus(split[0], split[1]);
                    }
                });
                battleFragment.startAnimation(toast, message);
                break;

            case "-cureteam":
                if (fromEffectId != null) {
                    switch (battleFragment.trimOrigin(fromEffect)) {
                        case "aromatherapy":
                            toAppendBuilder.append("沁人心脾的香气在场地上扩散开了!");
                            break;

                        case "healbell":
                            toAppendBuilder.append("铃声响起!");
                            break;
                    }
                } else {
                    attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0]);
                    toAppendBuilder.append(attackerOutputName);
                    toAppendBuilder.append("的队伍被治愈了");
                }
                logMessage = new SpannableStringBuilder(toAppendBuilder);
                toast = battleFragment.makeMinorToast(logMessage);
                toast.addListener(new AnimatorListenerWithNet() {
                    @Override
                    public void onAnimationStartWithNet(Animator animation) {
                        String[] teammate;
                        if (split[0].startsWith("p1")) {
                            teammate = BattleFragment.TEAMMATES[0];
                        } else {
                            teammate = BattleFragment.TEAMMATES[1];
                        }
                        for (String mate : teammate) {
                            for (String stt : BattleFragment.STTUS) {
                                battleFragment.removeAddonStatus(mate, stt);
                            }
                        }
                    }
                });
                battleFragment.startAnimation(toast, message);
                break;

            case "-item":
                attackerOutputName = translatePokemon(battleFragment.getPrintableOutputPokemonSide(split[0]));
                final String item;
                item = translateAbility(battleFragment.getPrintable(split[1]));
                if (fromEffect != null) {
                    // not to deal with item: or ability: or move:
                    switch (battleFragment.trimOrigin(fromEffect)) {
                        case "recycle":
                        case "pickup":
                            toAppendBuilder.append(attackerOutputName).append(" 回收了一个 ").append(item).append("!");
                            break;

                        case "frisk":
                            toAppendBuilder.append(battleFragment.getPrintableOutputPokemonSide(ofSource))
                                    .append("察觉到了对手的").append(item).append("!");
                            break;

                        case "thief":
                        case "covet":
                            defenderOutputName = battleFragment.getPrintableOutputPokemonSide(ofSource, false);
                            toAppendBuilder.append(attackerOutputName).append("偷走了 ").append(defenderOutputName).append("的").append(item).append("!");
                            break;

                        case "harvest":
                            toAppendBuilder.append(attackerOutputName).append(" 回收了一个 ").append(item).append("!");
                            break;

                        case "bestow":
                            defenderOutputName = translatePokemon(battleFragment.getPrintableOutputPokemonSide(ofSource, false));
                            toAppendBuilder.append(attackerOutputName).append("从").append(defenderOutputName).append(" 收到了 ").append(item).append("!");
                            break;

                        default:
                            toAppendBuilder.append(attackerOutputName).append("获得了一个 ").append(item).append(".");
                            break;
                    }
                    logMessage = new SpannableString(toAppendBuilder);
                    toast = battleFragment.makeMinorToast(logMessage);
                    animatorSet = battleFragment.createFlyingMessage(split[0], toast, new SpannableString(item));
                    battleFragment.startAnimation(animatorSet, message);
                } else {
                    switch (item) {
                        case "Air Balloon":
                            toAppendBuilder.append(attackerOutputName).append("靠着气球浮在了空中!");
                            break;

                        default:
                            toAppendBuilder.append(attackerOutputName).append("拥有 ").append(item).append("!");
                            break;
                    }
                    logMessage = new SpannableString(toAppendBuilder);
                    toast = battleFragment.makeMinorToast(logMessage);
                    toast.addListener(new AnimatorListenerWithNet() {
                        @Override
                        public void onAnimationStartWithNet(Animator animation) {
                            battleFragment.setAddonStatus(split[0], item);
                        }
                    });
                    battleFragment.startAnimation(toast, message);
                }
                pokemonInfo = battleFragment.getPokemonInfo(split[0]);
                pokemonInfo.setItem(item);
                break;

            case "-enditem":
                eat = messageDetails.contains("[eat]");
                weaken = messageDetails.contains("[weaken]");
                attackerOutputName = translatePokemon(battleFragment.getPrintableOutputPokemonSide(split[0]));
                item = translateAbility(split[1].trim());

                if (eat) {
                    toAppendBuilder.append(attackerOutputName).append(" 吃掉了它的").append(item).append("!");
                } else if (weaken) {
                    toAppendBuilder.append(attackerOutputName).append(" weakened the damage to ").append(item).append("!");
                } else if (fromEffect != null) {
                    switch (battleFragment.trimOrigin(fromEffect)) {
                        case "fling":
                            toAppendBuilder.append(attackerOutputName).append("投掷了它的").append(item).append("!");
                            break;

                        case "knockoff":
                            defenderOutputName = translatePokemon(battleFragment.getPrintableOutputPokemonSide(ofSource));
                            attackerOutputName = translatePokemon(battleFragment.getPrintableOutputPokemonSide(split[0], false));

                            toAppendBuilder.append(defenderOutputName).append("拍落了").append(attackerOutputName).append("的").append(item).append("!");
                            break;

                        case "stealeat":
                            defenderOutputName = battleFragment.getPrintableOutputPokemonSide(ofSource);
                            toAppendBuilder.append(defenderOutputName).append(" 偷走并吃掉了对手的").append(item).append("!");
                            break;

                        case "gem":
                            separator = messageDetails.indexOf("[move]");
                            move = "";
                            if (separator != -1) {
                                move = messageDetails.substring(separator + 6);
                                if (move.contains("|")) {
                                    move = move.substring(0, move.indexOf("|"));
                                }
                                move = translateMove(move);
                            }
                            toAppendBuilder.append("The ").append(item).append("强化了").append(move).append("的威力!");
                            break;

                        case "incinerate":
                            toAppendBuilder.append(attackerOutputName).append("'s ").append(item).append("被烧尽了!");
                            break;

                        default:
                            toAppendBuilder.append(attackerOutputName).append("失去了它的").append(item).append("!");
                            break;
                    }
                } else {
                    String itemId = MyApplication.toId(item);
                    switch (itemId) {
                        case "airballoon":
                            toAppendBuilder.append(attackerOutputName).append("的气球爆炸了");
                            break;

                        case "focussash":
                            toAppendBuilder.append(attackerOutputName).append(" 靠着它的气势披带撑住了！");
                            break;

                        case "focusband":
                            toAppendBuilder.append(attackerOutputName).append("靠着它的气势头带撑住了!");
                            break;

                        case "mentalherb":
                            toAppendBuilder.append(attackerOutputName).append("使用精神香草恢复了清醒！");
                            break;

                        case "whiteherb":
                            toAppendBuilder.append(attackerOutputName).append("使用它的白色香草回复了它的能力!");
                            break;

                        case "ejectbutton":
                            toAppendBuilder.append(attackerOutputName).append("因为逃脱按键被换下了！");
                            break;

                        case "redcard":
                            defenderOutputName = translatePokemon(battleFragment.getPrintableOutputPokemonSide(ofSource, false));
                            toAppendBuilder.append(attackerOutputName).append("向").append(defenderOutputName).append("出示了红牌!");
                            break;

                        default:
                            toAppendBuilder.append(attackerOutputName).append("的").append(item).append("生效了!");
                            break;
                    }
                }

                logMessage = new SpannableString(toAppendBuilder);
                toast = battleFragment.makeMinorToast(logMessage);
                animatorSet = battleFragment.createFlyingMessage(split[0], toast, new SpannableString(item));
                animatorSet.addListener(new AnimatorListenerWithNet() {
                    @Override
                    public void onAnimationStartWithNet(Animator animation) {
                        PokemonInfo pkm = battleFragment.getPokemonInfo(split[0]);
                        pkm.setItem(null);
                        battleFragment.removeAddonStatus(split[0], item);
                    }
                });
                battleFragment.startAnimation(animatorSet, message);
                break;

            case "-ability":
                attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0]);
                ability = translateAbility(split[1]);

                if (fromEffect != null) {
                    switch (battleFragment.trimOrigin(fromEffect)) {
                        case "trace":
                            defenderOutputName = translatePokemon(battleFragment.getPrintableOutputPokemonSide(ofSource, false));
                            toAppendBuilder.append(attackerOutputName).append("复制了").append(defenderOutputName).append("的").append(translateAbility(battleFragment.getPrintable(ability))).append("!");
                            break;

                        case "roleplay":
                            defenderOutputName = translatePokemon(battleFragment.getPrintableOutputPokemonSide(ofSource, false));
                            toAppendBuilder.append(attackerOutputName).append(" 复制了 ").append(defenderOutputName).append("'s ").append(translateAbility(battleFragment.getPrintable(ability))).append("!");
                            break;

                        case "mummy":
                            toAppendBuilder.append(attackerOutputName).append("的特性变成了木乃伊!");
                            break;

                        case "desolateland":
                            if (messageDetails.contains("[fail]")) {
                                toAppendBuilder.append("[").append(attackerOutputName).append("'s ").append(ability).append("] 强烈的阳光丝毫没有被削弱!");
                            }
                            break;
                        case "primordialsea":
                            if (messageDetails.contains("[fail]")) {
                                toAppendBuilder.append("[").append(attackerOutputName).append("'s ").append(ability).append("] 这场大雨仍然在倾泻着!");
                            }
                            break;
                        case "deltastream":
                            if (messageDetails.contains("[fail]")) {
                                toAppendBuilder.append("[").append(attackerOutputName).append("'s ").append(ability).append("] 神秘的气流仍然在持续着!");
                            }
                            break;

                        default:
                            toAppendBuilder.append(attackerOutputName).append(" acquired ").append(ability).append("!");
                            break;
                    }
                    pokemonInfo = battleFragment.getPokemonInfo(split[0]);
                    if (pokemonInfo != null) {
                        pokemonInfo.setAbility(MyApplication.toId(battleFragment.getPrintable(ability)));
                    }
                    PokemonInfo defInfo = battleFragment.getPokemonInfo(fromEffect);
                    if (defInfo != null) {
                        defInfo.setAbility(MyApplication.toId(battleFragment.getPrintable(ability)));
                    }
                } else {
                    switch (MyApplication.toId(ability)) {
                        case "pressure":
                            toAppendBuilder.append(attackerOutputName).append("正在施加压迫感!");
                            break;

                        case "moldbreaker":
                            toAppendBuilder.append(attackerOutputName).append("打破了常规!");
                            break;

                        case "turboblaze":
                            toAppendBuilder.append(attackerOutputName).append("正在释放溅射气场!");
                            break;

                        case "teravolt":
                            toAppendBuilder.append(attackerOutputName).append("正在释放炽热气场！");
                            break;

                        case "intimidate":
                            toAppendBuilder.append(attackerOutputName).append("的威吓!");
                            break;

                        case "unnerve":
                            if (split[0].startsWith("p2")) {
                                side = "你的队伍";
                            } else {
                                side = "对方的队伍";
                            }
                            toAppendBuilder.append(attackerOutputName).append("的紧张感让").append(side).append("紧张得吃不下树果！、!");
                            break;

                        case "aurabreak":
                            toAppendBuilder.append(attackerOutputName).append("反转了所有其它宝可梦的气场!");
                            break;

                        case "fairyaura":
                            toAppendBuilder.append(attackerOutputName).append("正在释放妖精气场！");
                            break;

                        case "darkaura":
                            toAppendBuilder.append(attackerOutputName).append(" 正在释放暗黑气场！");
                            break;

                        case "airlock":
                        case "cloudnine":
                            toAppendBuilder.append("天气的影响消失了.");
                            break;

                        default:
                            toAppendBuilder.append(attackerOutputName).append("有").append(translateAbility(battleFragment.getPrintable(ability))).append("特性!");
                            break;
                    }
                    pokemonInfo = battleFragment.getPokemonInfo(split[0]);
                    if (pokemonInfo != null) {
                        pokemonInfo.setAbility(MyApplication.toId(battleFragment.getPrintable(ability)));
                    }
                }
                logMessage = new SpannableString(toAppendBuilder);
                toast = battleFragment.makeToast(logMessage);
                battleFragment.startAnimation(toast, message);
                break;

            case "-endability":
                attackerOutputName = translatePokemon(battleFragment.getPrintableOutputPokemonSide(split[0]));
                ability = translateAbility(split[1]);

                if (fromEffect != null) {
                    switch (battleFragment.trimOrigin(fromEffect)) {
                        case "mummy":
                            attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0], false);
                            toAppendBuilder.append("(").append(attackerOutputName).append("'s 的特性原来是").append(translateAbility(battleFragment.getPrintable(ability))).append(")");
                            pokemonInfo = battleFragment.getPokemonInfo(split[0]);
                            if (pokemonInfo != null) {
                                pokemonInfo.setAbility(MyApplication.toId(battleFragment.getPrintable(ability)));
                            }
                            break;

                        default:
                            toAppendBuilder.append(attackerOutputName).append("\\的特性消失了！");
                            break;
                    }
                }
                logMessage = new SpannableString(toAppendBuilder);
                toast = battleFragment.makeToast(logMessage);
                battleFragment.startAnimation(toast, message);
                break;

            case "-transform":
                attacker = translatePokemon(battleFragment.getPrintableOutputPokemonSide(split[0]));
                defender = translatePokemon(battleFragment.getPrintable(split[1]));
                toAppend = attacker + "变成了" + defender + "!";
                logMessage = new SpannableString(toAppend);
                toast = battleFragment.makeMinorToast(logMessage);
                toast.addListener(new AnimatorListenerWithNet() {
                    @Override
                    public void onAnimationStartWithNet(Animator animation) {
                        if (battleFragment.getView() == null) {
                            return;
                        }
                        ImageView orgn = (ImageView) battleFragment.getView().findViewById(battleFragment.getSpriteId(split[0]));
                        ImageView dest = (ImageView) battleFragment.getView().findViewById(battleFragment.getSpriteId(split[1]));
                        orgn.setImageDrawable(dest.getDrawable());
                        battleFragment.copyBoost(split[1], split[0]);
                    }
                });
                battleFragment.startAnimation(toast, message);
                break;

            case "-formechange":
                final String oldForm = split[0];
                final String oldFormPrintable = battleFragment.getPrintableOutputPokemonSide(split[0]);
                final String newForm = split[1];

                final String newFormPrintable = battleFragment.getPrintableOutputPokemonSide(split[1]);
                switch (MyApplication.toId(newFormPrintable)) {
                    case "darmanitanzen":
                        toAppend = "达摩模式启动!";
                        break;
                    case "darmanitan":
                        toAppend = "达摩模式结束!";
                        break;
                    case "aegislashblade":
                        toAppend = "变成了刀剑形态!";
                        break;
                    case "aegislash":
                        toAppend = "变成了盾牌形态!";
                        break;
                    default:
                        toAppend = "变成了"+ oldForm + " 形态!";
                        break;
                }

                String position = split[0].substring(0, 3);
                battleFragment.formChange(position, newFormPrintable);
                pokemonInfo = battleFragment.getPokemonInfo(position);

                logMessage = new SpannableString(toAppend);
                toast = battleFragment.makeToast(logMessage, BattleFragment.ANIMATION_SHORT);

                toast.addListener(new AnimatorListenerWithNet() {
                    @Override
                    public void onAnimationStartWithNet(Animator animation) {
                        if (battleFragment.getView() == null) {
                            return;
                        }

                        boolean back = split[0].startsWith("p1");
                        ImageView sprite = (ImageView) battleFragment.getView().findViewById(battleFragment.getSpriteId(oldForm));
                        sprite.setImageResource(Pokemon.getPokemonSprite(battleFragment.getActivity(),
                                MyApplication.toId(newFormPrintable), back, pokemonInfo.isFemale(), pokemonInfo.isShiny()));
                        ImageView icon = (ImageView) battleFragment.getView().findViewById(battleFragment.getIconId(oldForm));
                        icon.setImageResource(Pokemon.getPokemonIcon(battleFragment.getActivity(),
                                MyApplication.toId(newFormPrintable)));
                    }
                });
                battleFragment.startAnimation(toast, message);
                break;

            case "-mega":
                attacker = translatePokemon(battleFragment.getPrintableOutputPokemonSide(split[0]));
                toAppendBuilder.append(attacker).append("Mega进化成为了Mega").append(split[1]).append("!");
                logMessage = new SpannableString(toAppendBuilder);
                toast = battleFragment.makeMinorToast(logMessage);
                battleFragment.startAnimation(toast, message);
                break;

            case "-start":
                attackerOutputName = translatePokemon(battleFragment.getPrintableOutputPokemonSide(split[0]));
                animatorSet = new AnimatorSet();
                final String newEffect;
                newEffect = battleFragment.getPrintable(split[1]);
                switch (MyApplication.toId(battleFragment.getPrintable(split[1]))) {
                    case "typechange":
                        if (fromEffect != null) {
                            if (battleFragment.trimOrigin(fromEffect).equals("reflecttype")) {
                                toAppendBuilder.append(attackerOutputName).append("的属性变得和").append(translatePokemon(battleFragment.getPrintable(ofSource))).append("一样了!");
                            } else {
                                toAppendBuilder.append(attackerOutputName).append("的").append(translateAbility(battleFragment.getPrintable(fromEffect))).append("使它变成了").append(battleFragment.getPrintable(split[2])).append(" 属性!");
                            }
                        } else {
                            toAppendBuilder.append(attackerOutputName).append("变成了").append(battleFragment.getPrintable(split[2])).append(" 属性!");
                        }
                        break;

                    case "typeadd":
                        attackerOutputName = translatePokemon(battleFragment.getPrintableOutputPokemonSide(split[0], false));
                        toAppendBuilder.append(battleFragment.getPrintable(split[2])).append(" 属性被添加给了").append(attackerOutputName).append("!");
                        break;

                    case "powertrick":
                        toAppendBuilder.append(attackerOutputName).append("交换了它的攻击和防御!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                battleFragment.setAddonStatus(split[0], newEffect);
                            }
                        });
                        break;

                    case "foresight":
                    case "miracleeye":
                        toAppendBuilder.append(attackerOutputName).append("被识破了!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                battleFragment.setAddonStatus(split[0], newEffect);
                            }
                        });
                        break;

                    case "telekinesis":
                        toAppendBuilder.append(attackerOutputName).append("被投向了空中!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                battleFragment.setAddonStatus(split[0], newEffect);
                            }
                        });
                        break;

                    case "confusion":
                        if (messageDetails.contains("[already]")) {
                            toAppendBuilder.append(attackerOutputName).append("已经混乱了！");
                        } else {
                            toAppendBuilder.append(attackerOutputName).append("混乱了!");
                            animatorSet.addListener(new AnimatorListenerWithNet() {
                                @Override
                                public void onAnimationStartWithNet(Animator animation) {
                                    battleFragment.setAddonStatus(split[0], newEffect);
                                }
                            });
                        }
                        break;

                    case "leechseed":
                        toAppendBuilder.append(attackerOutputName).append("被种下了种子!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                battleFragment.setAddonStatus(split[0], newEffect);
                            }
                        });
                        break;

                    case "mudsport":
                        toAppendBuilder.append("电系的威力降低了!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                battleFragment.setAddonStatus(split[0], newEffect);
                            }
                        });
                        break;

                    case "watersport":
                        toAppendBuilder.append("火系的威力降低了!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                battleFragment.setAddonStatus(split[0], newEffect);
                            }
                        });
                        break;

                    case "yawn":
                        toAppendBuilder.append(attackerOutputName).append("感到昏昏欲睡!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                battleFragment.setAddonStatus(split[0], newEffect);
                            }
                        });
                        break;

                    case "flashfire":
                        attackerOutputName = translatePokemon(battleFragment.getPrintableOutputPokemonSide(split[0], false));
                        toAppendBuilder.append(attackerOutputName).append("的火系招式威力提升了！");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                battleFragment.setAddonStatus(split[0], newEffect);
                            }
                        });
                        break;

                    case "taunt":
                        toAppendBuilder.append(attackerOutputName).append("中了挑拨!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                battleFragment.setAddonStatus(split[0], newEffect);
                            }
                        });
                        break;

                    case "imprison":
                        toAppendBuilder.append(attackerOutputName).append("封印了对手的招式!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                battleFragment.setAddonStatus(split[0], newEffect);
                            }
                        });
                        break;

                    case "disable":
                        toAppendBuilder.append(attackerOutputName).append("的").append(translateMove(battleFragment.getPrintable(split[2]))).append("无法使用了!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                battleFragment.setAddonStatus(split[0], newEffect);
                            }
                        });
                        break;

                    case "embargo":
                        toAppendBuilder.append(attackerOutputName).append("无法使用道具了!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                battleFragment.setAddonStatus(split[0], newEffect);
                            }
                        });
                        break;

                    case "ingrain":
                        toAppendBuilder.append(attackerOutputName).append("扎下了根!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                battleFragment.setAddonStatus(split[0], newEffect);
                            }
                        });
                        break;

                    case "aquaring":
                        toAppendBuilder.append(attackerOutputName).append("用水幕包围了自己!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                battleFragment.setAddonStatus(split[0], newEffect);
                            }
                        });
                        break;

                    case "stockpile1":
                        toAppendBuilder.append(attackerOutputName).append("蓄力了1次!");
                        break;

                    case "stockpile2":
                        toAppendBuilder.append(attackerOutputName).append("蓄力了2次!");
                        break;

                    case "stockpile3":
                        toAppendBuilder.append(attackerOutputName).append("蓄力了3次!");
                        break;

                    case "perish0":
                        toAppendBuilder.append("距离").append(attackerOutputName).append("的灭亡还剩0回合。");
                        break;

                    case "perish1":
                        toAppendBuilder.append("距离").append(attackerOutputName).append("的灭亡还剩1回合。");
                        break;

                    case "perish2":
                        toAppendBuilder.append("距离").append(attackerOutputName).append("的灭亡还剩2回合。");
                        break;

                    case "perish3":
                        toAppendBuilder.append("距离").append(attackerOutputName).append("的灭亡还剩3回合。");
                        break;

                    case "encore":
                        toAppendBuilder.append(attackerOutputName).append("收到了再来一次!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                battleFragment.setAddonStatus(split[0], newEffect);
                            }
                        });
                        break;

                    case "bide":
                        toAppendBuilder.append(attackerOutputName).append("正在贮存能量!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                battleFragment.setAddonStatus(split[0], newEffect);
                            }
                        });
                        break;

                    case "slowstart":
                        toAppendBuilder.append(attackerOutputName).append("因为缓慢启动，行动变得迟缓!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                battleFragment.setAddonStatus(split[0], newEffect);
                            }
                        });
                        break;

                    case "attract":
                        if (fromEffect != null) {
                            toAppendBuilder.append(attackerOutputName).append("爱上了").append(battleFragment.getPrintable(fromEffect)).append("!");
                        } else {
                            toAppendBuilder.append(attackerOutputName).append("坠入爱河！");
                        }
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                battleFragment.setAddonStatus(split[0], newEffect);
                            }
                        });
                        break;

                    case "autotomize":
                        toAppendBuilder.append(attackerOutputName).append("变得灵活了!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                battleFragment.setAddonStatus(split[0], newEffect);
                            }
                        });
                        break;

                    case "focusenergy":
                        toAppendBuilder.append(attackerOutputName).append("正在集气!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                battleFragment.setAddonStatus(split[0], newEffect);
                            }
                        });
                        break;

                    case "curse":
                        attackerOutputName = translatePokemon(battleFragment.getPrintableOutputPokemonSide(split[0], false));
                        toAppendBuilder.append(translatePokemon(battleFragment.getPrintableOutputPokemonSide(ofSource))).append("缩减了自身的HP并且附加了诅咒给").append(attackerOutputName).append("!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                battleFragment.setAddonStatus(split[0], newEffect);
                            }
                        });
                        break;

                    case "nightmare":
                        toAppendBuilder.append(attackerOutputName).append("正在做噩梦！");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                battleFragment.setAddonStatus(split[0], newEffect);
                            }
                        });
                        break;

                    case "magnetrise":
                        toAppendBuilder.append(attackerOutputName).append("通过电磁力使自己悬浮了起来！!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                battleFragment.setAddonStatus(split[0], newEffect);
                            }
                        });
                        break;

                    case "smackdown":
                        toAppendBuilder.append(attackerOutputName).append("被击落！");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                battleFragment.setAddonStatus(split[0], newEffect);
                            }
                        });
                        break;

                    case "substitute":
                        if (messageDetails.contains("[damage]")) {
                            attackerOutputName = translatePokemon(battleFragment.getPrintableOutputPokemonSide(split[0], false));
                            toAppendBuilder.append("替身代替").append(attackerOutputName).append("承受了伤害!");
                        } else if (messageDetails.contains("[block]")) {
                            toAppendBuilder.append("但是失败了!");
                        } else if (messageDetails.contains("[already]")) {
                            toAppendBuilder.append(attackerOutputName).append("已经有一个替身了!");
                        } else {
                            toAppendBuilder.append(attackerOutputName).append("制造了一个替身!");
                            animatorSet.addListener(new AnimatorListenerWithNet() {
                                @Override
                                public void onAnimationStartWithNet(Animator animation) {
                                    if (battleFragment.getView() == null) {
                                        return;
                                    }
                                    ImageView imageView = (ImageView) battleFragment.getView().findViewById(battleFragment.getSpriteId(split[0]));
                                    imageView.setAlpha(0.2f);
                                    ImageView substitute = new ImageView(battleFragment.getActivity());
                                    substitute.setImageResource(battleFragment.getSubstitute(split[0]));
                                    substitute.setTag("Substitute");

                                    RelativeLayout relativeLayout = (RelativeLayout) battleFragment.getView().findViewById(battleFragment.getPkmLayoutId(split[0]));
                                    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                                    layoutParams.addRule(RelativeLayout.ALIGN_TOP, battleFragment.getSpriteId(split[0]));
                                    layoutParams.addRule(RelativeLayout.ALIGN_LEFT, battleFragment.getSpriteId(split[0]));
                                    relativeLayout.addView(substitute, layoutParams);
                                }
                            });
                        }
                        break;

                    case "uproar":
                        if (messageDetails.contains("[upkeep]")) {
                            toAppendBuilder.append(attackerOutputName).append(" 正在制造噪音！");
                        } else {
                            toAppendBuilder.append(attackerOutputName).append("引发了噪音!");
                        }
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                battleFragment.setAddonStatus(split[0], newEffect);
                            }
                        });
                        break;

                    case "doomdesire":
                        toAppendBuilder.append(attackerOutputName).append("选择了破灭的未来作为它的命运！!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                battleFragment.setAddonStatus(split[0], newEffect);
                            }
                        });
                        break;

                    case "futuresight":
                        toAppendBuilder.append(attackerOutputName).append("预知了一次攻击!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                battleFragment.setAddonStatus(split[0], newEffect);
                            }
                        });
                        break;

                    case "mimic":
                        toAppendBuilder.append(attackerOutputName).append(" 学习了 ").append(translateMove(battleFragment.getPrintable(split[2]))).append("!");
                        break;

                    case "followme":
                    case "ragepowder":
                        toAppendBuilder.append(attackerOutputName).append("成为了注意力的焦点！");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                battleFragment.setAddonStatus(split[0], newEffect);
                            }
                        });
                        break;

                    case "powder":
                        toAppendBuilder.append(attackerOutputName).append("被包裹在粉尘中!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                battleFragment.setAddonStatus(split[0], newEffect);
                            }
                        });
                        break;

                    default:
                        toAppendBuilder.append(attackerOutputName).append("的 ").append(translateAbility(battleFragment.getPrintable(split[1]))).append("发动了!");
                        break;
                }
                logMessage = new SpannableString(toAppendBuilder);
                toast = battleFragment.makeMinorToast(logMessage);
                animatorSet.play(toast);
                battleFragment.startAnimation(animatorSet, message);
                break;

            case "-end":
                attacker = split[0];
                attackerOutputName = translatePokemon(battleFragment.getPrintableOutputPokemonSide(split[0]));
                animatorSet = new AnimatorSet();
                newEffect = battleFragment.getPrintable(split[1]);
                animatorSet.addListener(new AnimatorListenerWithNet() {
                    @Override
                    public void onAnimationStartWithNet(Animator animation) {
                        battleFragment.removeAddonStatus(split[0], newEffect);
                    }
                });
                switch (MyApplication.toId(battleFragment.getPrintable(split[1]))) {
                    case "powertrick":
                        toAppendBuilder.append(attackerOutputName).append("交换了它的攻击和防御!");
                        break;

                    case "telekinesis":
                        toAppendBuilder.append(attackerOutputName).append("从意念移物状态中逃脱了！");
                        break;

                    case "confusion":
                        if (attacker.startsWith("p2")) {
                            toAppendBuilder.append(attackerOutputName).append("解除了混乱状态！");
                        } else {
                            toAppendBuilder.append(attackerOutputName).append("解除了混乱状态！");
                        }
                        break;

                    case "leechseed":
                        if (fromEffect != null && fromEffectId.equals("rapidspin")) {
                            toAppendBuilder.append(attackerOutputName).append("摆脱了寄生种子！");
                        }
                        break;

                    case "healblock":
                        toAppendBuilder.append(attackerOutputName).append("的回复封印解除了！");
                        break;

                    case "taunt":
                        toAppendBuilder.append(attackerOutputName).append("的挑拨状态解除了！");
                        break;

                    case "disable":
                        toAppendBuilder.append(attackerOutputName).append("不再被封印了!");
                        break;

                    case "embargo":
                        toAppendBuilder.append(attackerOutputName).append("可以使用道具了!");
                        break;

                    case "torment":
                        toAppendBuilder.append(attackerOutputName).append("诅咒状态解除了!");
                        break;

                    case "encore":
                        toAppendBuilder.append(attackerOutputName).append("的鼓掌状态解除了!");
                        break;

                    case "bide":
                        toAppendBuilder.append(attackerOutputName).append("释放了自身的能量!");
                        break;

                    case "magnetrise":
                        if (attacker.startsWith("p2")) {
                            toAppendBuilder.append(attackerOutputName).append("的电磁悬浮状态解除了!");
                        } else {
                            toAppendBuilder.append(attackerOutputName).append("的电磁悬浮状态解除了!");
                        }
                        break;

                    case "perishsong":
                        break;

                    case "substitute":
                        toAppendBuilder.append(attackerOutputName).append("的替身消失了!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                if (battleFragment.getView() == null) {
                                    return;
                                }
                                RelativeLayout relativeLayout = (RelativeLayout) battleFragment.getView().findViewById(battleFragment.getPkmLayoutId(split[0]));
                                View v = relativeLayout.findViewWithTag("Substitute");
                                if (v != null) {
                                    relativeLayout.removeView(v);
                                }
                                ImageView imageView = (ImageView) battleFragment.getView().findViewById(battleFragment.getSpriteId(split[0]));
                                imageView.setAlpha(1f);
                            }
                        });
                        break;

                    case "uproar":
                        toAppendBuilder.append(attackerOutputName).append("冷静下来了.");
                        break;

                    case "stockpile":
                        toAppendBuilder.append(attackerOutputName).append("力量储存的效果消失了！");
                        break;

                    case "infestation":
                        toAppendBuilder.append(attackerOutputName).append("从缠身状态逃脱了!");
                        break;

                    default:
                        if (split[1].contains("move:")) {
                            toAppendBuilder.append(attackerOutputName).append("接收了").append(translateMove(battleFragment.getPrintable(split[1]))).append(" 的攻击!");
                        } else {
                            toAppendBuilder.append(attackerOutputName).append("的 ").append(translateAbility(battleFragment.getPrintable(split[1]))).append(" 状态结束了!");
                        }
                        break;
                }
                logMessage = new SpannableString(toAppendBuilder);
                toast = battleFragment.makeMinorToast(logMessage);
                animatorSet.play(toast);
                battleFragment.startAnimation(animatorSet, message);
                break;

            case "-singleturn":
                attackerOutputName = translatePokemon(battleFragment.getPrintableOutputPokemonSide(split[0]));
                switch (MyApplication.toId(battleFragment.getPrintable(split[1]))) {
                    case "roost":
                        toAppendBuilder.append(attackerOutputName).append("降落到了地面上!");
                        break;

                    case "quickguard":
                        attackerOutputName = translatePokemon(battleFragment.getPrintableOutputPokemonSide(split[0], false));
                        toAppendBuilder.append("快速防守保护了").append(attackerOutputName).append(" ????");
                        break;

                    case "wideguard":
                        attackerOutputName = translatePokemon(battleFragment.getPrintableOutputPokemonSide(split[0], false));
                        toAppendBuilder.append("广域防守保护了").append(attackerOutputName).append(" !");
                        break;

                    case "protect":
                        toAppendBuilder.append(attackerOutputName).append("保护了自己!");
                        break;

                    case "endure":
                        toAppendBuilder.append(attackerOutputName).append(" 坚持了下来!");
                        break;

                    case "helpinghand":
                        attackerOutputName = translatePokemon(battleFragment.getPrintableOutputPokemonSide(split[0], false));
                        toAppendBuilder.append(battleFragment.getPrintableOutputPokemonSide(ofSource)).append("准备帮助").append(attackerOutputName).append("!");
                        break;

                    case "focuspunch":
                        toAppendBuilder.append(attackerOutputName).append("正在集中注意力!");
                        break;

                    case "snatch":
                        toAppendBuilder.append(attackerOutputName).append("等待一个目标使用技能!");
                        break;

                    case "magiccoat":
                        toAppendBuilder.append(attackerOutputName).append(" 给自己裹上了一层魔术外衣！");
                        break;

                    case "matblock":
                        toAppendBuilder.append(attackerOutputName).append("举起了一块榻榻米挡下了即将到来的攻击！");
                        break;

                    case "electrify":
                        toAppendBuilder.append(attackerOutputName).append("的技能带电了！");
                        break;
                }
                logMessage = new SpannableString(toAppendBuilder);
                toast = battleFragment.makeMinorToast(logMessage);
                animatorSet = battleFragment.createFlyingMessage(split[0], toast, new SpannableString(battleFragment.getPrintable(split[1])));
                battleFragment.startAnimation(animatorSet, message);
                break;

            case "-singlemove":
                attackerOutputName = translatePokemon(battleFragment.getPrintableOutputPokemonSide(split[0]));
                switch (MyApplication.toId(battleFragment.getPrintable(split[1]))) {
                    case "grudge":
                        toAppendBuilder.append(attackerOutputName).append("想向对手释放怨念!");
                        break;
                    case "destinybond":
                        toAppendBuilder.append(attackerOutputName).append("想和对手同归于尽!");
                        break;
                }
                logMessage = new SpannableString(toAppendBuilder);
                toast = battleFragment.makeMinorToast(logMessage);
                animatorSet = battleFragment.createFlyingMessage(split[0], toast, new SpannableString(battleFragment.getPrintable(split[1])));
                battleFragment.startAnimation(animatorSet, message);
                break;

            case "-activate":
                attacker = split[0];
                attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0]);
                switch (MyApplication.toId(battleFragment.getPrintable(split[1]))) {
                    case "confusion":
                        toAppendBuilder.append(attackerOutputName).append("混乱了!");
                        break;

                    case "destinybond":
                        toAppendBuilder.append(attackerOutputName).append("和对手同归于尽了!");
                        break;

                    case "snatch":
                        toAppendBuilder.append(attackerOutputName).append("抢夺了").append(translatePokemon(battleFragment.getPrintable(ofSource))).append("的技能!");
                        break;

                    case "grudge":
                        toAppendBuilder.append(attackerOutputName).append("的").append(translateMove(battleFragment.getPrintable(split[2]))).append("因为怨恨失去了所有的PP!");
                        break;

                    case "quickguard":
                        attackerOutputName = translatePokemon(battleFragment.getPrintableOutputPokemonSide(split[0], false));
                        toAppendBuilder.append("快速防守保护了").append(attackerOutputName).append("!");
                        break;

                    case "wideguard":
                        attackerOutputName = translatePokemon(battleFragment.getPrintableOutputPokemonSide(split[0], false));
                        toAppendBuilder.append("广域防守保护了").append(attackerOutputName).append("!");
                        break;

                    case "protect":
                        toAppendBuilder.append(attackerOutputName).append("保护了自身!");
                        break;

                    case "substitute":
                        if (messageDetails.contains("[damage]")) {
                            attackerOutputName = translatePokemon(battleFragment.getPrintableOutputPokemonSide(split[0], false));
                            toAppendBuilder.append("替身代替").append(attackerOutputName).append(" 受到了伤害!");
                        } else if (messageDetails.contains("[block]")) {
                            toAppendBuilder.append(attackerOutputName).append("'s Substitute blocked").append(battleFragment.getPrintable(split[2])).append("!");
                        }
                        break;

                    case "attract":
                        toAppendBuilder.append(attackerOutputName).append("爱上了").append(translatePokemon(battleFragment.getPrintable(ofSource))).append("!");
                        break;

                    case "bide":
                        toAppendBuilder.append(attackerOutputName).append("正在储存能量!");
                        break;

                    case "mist":
                        toAppendBuilder.append(attackerOutputName).append("被迷雾保护了!");
                        break;

                    case "trapped":
                        toAppendBuilder.append(attackerOutputName).append("再也无法逃脱了!");
                        break;

                    case "stickyweb":
                        toAppendBuilder.append(attackerOutputName).append("被黏黏网缠住了!");
                        break;

                    case "happyhour":
                        toAppendBuilder.append("大家被欢乐的气氛包围了!");
                        break;

                    case "celebrate":
                        if (attacker.startsWith("p2")) {
                            side = battleFragment.getPlayer2();
                        } else {
                            side = battleFragment.getPlayer1();
                        }
                        toAppendBuilder.append("恭喜你, ").append(side).append("!");
                        break;

                    case "trick":
                    case "switcheroo":
                        toAppendBuilder.append(attackerOutputName).append("交换了上方的道具!");
                        break;

                    case "brickbreak":
                        if (MyApplication.toId(ofSource).startsWith("p2")) {
                            side = "对方队伍";
                        } else {
                            side = "我方队伍";
                        }
                        toAppendBuilder.append(attackerOutputName).append(" 击碎了 ").append(side).append(" 保护!");
                        break;

                    case "pursuit":
                        toAppendBuilder.append(attackerOutputName).append("正在准备收回!");
                        break;

                    case "feint":
                        toAppendBuilder.append(attackerOutputName).append("摔倒了!");
                        break;

                    case "spite":
                        toAppendBuilder.append(attackerOutputName).append("的").append(translateMove(battleFragment.getPrintable(split[2]))).append("的PP减少了").append("!");
                        break;

                    case "gravity":
                        toAppendBuilder.append(attackerOutputName).append("因为重力不能停留空中!");
                        break;

                    case "magnitude":
                        toAppendBuilder.append("震级变化 ").append(battleFragment.getPrintable(split[2])).append("!");
                        break;

                    case "sketch":
                        toAppendBuilder.append(attackerOutputName).append(" sketched ").append(battleFragment.getPrintable(split[2])).append("!");
                        break;

                    case "skillswap":
                        toAppendBuilder.append(attackerOutputName).append("与目标交换了特性!");
                        if (ofSource != null) {
                            toAppendBuilder.append("\n").append(attackerOutputName).append(" acquired ").append(translateAbility(battleFragment.getPrintable(split[2]))).append("!");
                            toAppendBuilder.append("\n").append(translatePokemon(battleFragment.getPrintable(ofSource))).append("获得了").append(translateAbility(battleFragment.getPrintable(split[3]))).append("!");
                        }
                        break;

                    case "charge":
                        toAppendBuilder.append(attackerOutputName).append("开始充电!");
                        break;

                    case "struggle":
                        toAppendBuilder.append(attackerOutputName).append("没有技能可以用了!");
                        break;

                    case "bind":
                        toAppendBuilder.append(attackerOutputName).append(" was squeezed by ").append(battleFragment.getPrintable(ofSource)).append("!");
                        break;

                    case "wrap":
                        toAppendBuilder.append(attackerOutputName).append("被 ").append(battleFragment.getPrintable(ofSource)).append("包住了!");
                        break;

                    case "clamp":
                        toAppendBuilder.append(translatePokemon(battleFragment.getPrintable(ofSource))).append(" clamped ").append(attackerOutputName).append("!");
                        break;

                    case "whirlpool":
                        toAppendBuilder.append(attackerOutputName).append("陷入了漩涡中！");
                        break;

                    case "firespin":
                        toAppendBuilder.append(attackerOutputName).append("陷入了火焰漩涡中！");
                        break;

                    case "magmastorm":
                        toAppendBuilder.append(attackerOutputName).append("陷入了岩浆的漩涡中！");
                        break;

                    case "sandtomb":
                        toAppendBuilder.append(attackerOutputName).append("陷入了流沙中！");
                        break;

                    case "infestation":
                        toAppendBuilder.append(attackerOutputName).append(" 受到了缠身的影响 ").append("!");
                        break;

                    case "afteryou":
                        toAppendBuilder.append(attackerOutputName).append("接受了好意！");
                        break;

                    case "quash":
                        toAppendBuilder.append(attackerOutputName).append("的行动延后了");
                        break;

                    case "powersplit":
                        toAppendBuilder.append(attackerOutputName).append("与目标平分了力量！");
                        break;

                    case "guardsplit":
                        toAppendBuilder.append(attackerOutputName).append("与目标平分了防御！");
                        break;

                    case "ingrain":
                        toAppendBuilder.append(attackerOutputName).append(" 扎下了根!");
                        break;

                    case "matblock":
                        toAppendBuilder.append(battleFragment.getPrintable(split[2])).append("被掀起的榻榻米挡住了！!");
                        break;

                    case "powder":
                        toAppendBuilder.append("当火焰接触到宝可梦身上的粉尘时发生了爆炸！");
                        break;

                    case "fairylock":
                        toAppendBuilder.append("下一回合结束前所有宝可梦都不能交换！");
                        break;

                    //abilities
                    case "sturdy":
                        toAppendBuilder.append(attackerOutputName).append("因为坚硬特性坚持住了!");
                        break;

                    case "magicbounce":
                    case "magiccoat":
                    case "rebound":
                        break;

                    case "wonderguard":
                        toAppendBuilder.append(attackerOutputName).append("的神奇守护抵御了攻击！");
                        break;

                    case "speedboost":
                        toAppendBuilder.append(attackerOutputName).append("的加速特性提升了它的速度!");
                        break;

                    case "forewarn":
                        toAppendBuilder.append(attackerOutputName).append("的预知梦特性让他知道了对手持有").append(translateMove(battleFragment.getPrintable(split[2]))).append("技能!");
                        break;

                    case "anticipation":
                        toAppendBuilder.append(attackerOutputName).append(" 颤抖了!");
                        break;

                    case "telepathy":
                        toAppendBuilder.append(attackerOutputName).append("回避了来自友方宝可梦的攻击！!");
                        break;

                    case "suctioncups":
                        toAppendBuilder.append(attackerOutputName).append("用吸盘粘在了地面上！!");
                        break;

                    case "symbiosis":
                        attackerOutputName = translatePokemon(battleFragment.getPrintableOutputPokemonSide(split[0], false));
                        toAppendBuilder.append(battleFragment.getPrintable(ofSource)).append("与").append(attackerOutputName).append("分享了").append(battleFragment.getPrintable(split[2]));
                        break;

                    //items
                    case "custapberry":
                    case "quickclaw":
                        toAppendBuilder.append(attackerOutputName).append("的 ").append(battleFragment.getPrintable(split[1])).append("让它提前行动!");
                        break;

                    case "leppaberry":
                        toAppendBuilder.append(attackerOutputName).append(" 使用Leppa Berry回复了").append(translateMove(battleFragment.getPrintable(split[2]))).append("的PP！");
                        break;

                    default:
                        toAppendBuilder.append(attackerOutputName).append("的 ").append(" 生效了!");
                        break;
                }
                logMessage = new SpannableString(toAppendBuilder);
                toast = battleFragment.makeMinorToast(logMessage);
                animatorSet = battleFragment.createFlyingMessage(split[0], toast, new SpannableString(battleFragment.getPrintable(split[1])));
                battleFragment.startAnimation(animatorSet, message);
                break;

            case "-sidestart":
                if (messageDetails.startsWith("p2")) {
                    side = "对方队伍";
                } else {
                    side = "我方队伍";
                }

                fromEffect = split[1];
                fromEffectId = MyApplication.toId(battleFragment.getPrintable(fromEffect));
                animatorSet = new AnimatorSet();
                switch (fromEffectId) {
                    case "stealthrock":
                        toAppendBuilder.append("尖锐的岩石漂浮在").append(side).append("的场地上!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                if (battleFragment.getView() == null) {
                                    return;
                                }
                                int id = (messageDetails.startsWith("p1")) ? R.id.field_rocks : R.id.field_rocks_o;
                                battleFragment.getView().findViewById(id).setVisibility(View.VISIBLE);
                            }
                        });
                        break;

                    case "spikes":
                        toAppendBuilder.append("地菱布满在了").append(side).append("周围!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                if (battleFragment.getView() == null) {
                                    return;
                                }
                                battleFragment.getView().findViewById(battleFragment.getLastVisibleSpike(messageDetails, true)).setVisibility(View.VISIBLE);
                            }
                        });
                        break;

                    case "toxicspikes":
                        toAppendBuilder.append("毒菱布满在了").append(side).append("的场地上!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                if (battleFragment.getView() == null) {
                                    return;
                                }
                                battleFragment.getView().findViewById(battleFragment.getLastVisibleTSpike(messageDetails, true)).setVisibility(View.VISIBLE);
                            }
                        });
                        break;

                    case "stickyweb":
                        toAppendBuilder.append("一张粘网展开在了").append(side).append("的场地上!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                if (battleFragment.getView() == null) {
                                    return;
                                }
                                int id = (messageDetails.startsWith("p1")) ? R.id.field_webs : R.id.field_webs_o;
                                battleFragment.getView().findViewById(id).setVisibility(View.VISIBLE);
                            }
                        });
                        break;

                    case "tailwind":
                        toAppendBuilder.append("顺风在").append(side).append("的身后展开了!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                if (battleFragment.getView() == null) {
                                    return;
                                }
                                ((TextView) battleFragment.getView().findViewById(R.id.tail_wind)).setText("Tailwind");
                            }
                        });
                        break;

                    case "reflect":
                        toAppendBuilder.append("反射盾增强了").append(side).append("的防御力!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                if (battleFragment.getView() == null) {
                                    return;
                                }
                                int id = (messageDetails.startsWith("p1")) ? R.id.field_reflect : R.id.field_reflect_o;
                                battleFragment.getView().findViewById(id).setVisibility(View.VISIBLE);
                            }
                        });
                        break;

                    case "lightscreen":
                        toAppendBuilder.append("光墙增强了").append(side).append("的特防!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                if (battleFragment.getView() == null) {
                                    return;
                                }
                                int id = (messageDetails.startsWith("p1")) ? R.id.field_lightscreen : R.id.field_lightscreen_o;
                                battleFragment.getView().findViewById(id).setVisibility(View.VISIBLE);
                            }
                        });
                        break;

                    case "auroraveil":
                        toAppendBuilder.append("极光幕使得对方队伍在面对物理和特殊攻击时更强了!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                if (battleFragment.getView() == null) {
                                    return;
                                }
                                int id = (messageDetails.startsWith("p1")) ? R.id.field_auroraveil : R.id.field_auroraveil_o;
                                battleFragment.getView().findViewById(id).setVisibility(View.VISIBLE);
                            }
                        });
                        break;

                    case "safeguard":
                        side = Character.toUpperCase(side.charAt(0)) + side.substring(1);
                        toAppendBuilder.append(side).append("被包围在神秘的守护之中");
                        break;

                    case "mist":
                        side = Character.toUpperCase(side.charAt(0)) + side.substring(1);
                        toAppendBuilder.append(side).append("被包围在了白雾之中");
                        break;

                    case "luckychant":
                        toAppendBuilder.append("幸运咒语保护了").append(side).append("不会遭受会心一击!");
                        break;

                    case "firepledge":
                        toAppendBuilder.append("A sea of fire enveloped ").append(side).append("!");
                        break;

                    case "waterpledge":
                        toAppendBuilder.append("A rainbow appeared in the sky on ").append(side).append("'s side!");
                        break;

                    case "grasspledge":
                        toAppendBuilder.append("A swamp enveloped ").append(side).append("!");
                        break;

                    default:
                        toAppendBuilder.append(battleFragment.getPrintable(fromEffect)).append(" started!");
                        break;
                }

                logMessage = new SpannableStringBuilder(toAppendBuilder);
                toast = battleFragment.makeMinorToast(logMessage);
                animatorSet.play(toast);
                battleFragment.startAnimation(animatorSet, message);
                break;

            case "-sideend":
                if (messageDetails.startsWith("p2")) {
                    side = "对方队伍";
                } else {
                    side = "我方队伍";
                }

                fromEffect = split[1];
                fromEffectId = MyApplication.toId(battleFragment.getPrintable(fromEffect));

                animatorSet = new AnimatorSet();
                switch (fromEffectId) {
                    case "stealthrock":
                        toAppendBuilder.append("尖锐的岩石从").append(side).append("的场地周围消失了!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                if (battleFragment.getView() == null) {
                                    return;
                                }
                                int id = (messageDetails.startsWith("p1")) ? R.id.field_rocks : R.id.field_rocks_o;
                                battleFragment.getView().findViewById(id).setVisibility(View.INVISIBLE);
                            }
                        });
                        break;

                    case "spikes":
                        toAppendBuilder.append("地菱从").append(side).append("的场地周围消失了!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                if (battleFragment.getView() == null) {
                                    return;
                                }
                                battleFragment.getView().findViewById(battleFragment.getLastVisibleSpike(messageDetails, false)).setVisibility(View.INVISIBLE);
                                battleFragment.getView().findViewById(battleFragment.getLastVisibleSpike(messageDetails, false)).setVisibility(View.INVISIBLE);
                                battleFragment.getView().findViewById(battleFragment.getLastVisibleSpike(messageDetails, false)).setVisibility(View.INVISIBLE);
                            }
                        });
                        break;

                    case "toxicspikes":
                        toAppendBuilder.append("毒菱从").append(side).append("的场地周围消失了!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                if (battleFragment.getView() == null) {
                                    return;
                                }
                                battleFragment.getView().findViewById(battleFragment.getLastVisibleTSpike(messageDetails, false)).setVisibility(View.INVISIBLE);
                                battleFragment.getView().findViewById(battleFragment.getLastVisibleTSpike(messageDetails, false)).setVisibility(View.INVISIBLE);
                            }
                        });
                        break;

                    case "stickyweb":
                        toAppendBuilder.append("虫网从").append(side).append("的场地下消失了!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                if (battleFragment.getView() == null) {
                                    return;
                                }
                                int id = (messageDetails.startsWith("p1")) ? R.id.field_webs : R.id.field_webs_o;
                                battleFragment.getView().findViewById(id).setVisibility(View.INVISIBLE);
                            }
                        });
                        break;

                    case "tailwind":
                        side = Character.toUpperCase(side.charAt(0)) + side.substring(1);
                        toAppendBuilder.append(side).append("的顺风停止了！");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                if (battleFragment.getView() == null) {
                                    return;
                                }
                                ((TextView) battleFragment.getView().findViewById(R.id.tail_wind)).setText(null);
                            }
                        });
                        break;

                    case "reflect":
                        side = Character.toUpperCase(side.charAt(0)) + side.substring(1);
                        toAppendBuilder.append(side).append("的反射盾消失了！");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                if (battleFragment.getView() == null) {
                                    return;
                                }
                                int id = (messageDetails.startsWith("p1")) ? R.id.field_reflect : R.id.field_reflect_o;
                                battleFragment.getView().findViewById(id).setVisibility(View.INVISIBLE);
                            }
                        });
                        break;

                    case "lightscreen":
                        side = Character.toUpperCase(side.charAt(0)) + side.substring(1);
                        toAppendBuilder.append(side).append("的光墙消失了！");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                if (battleFragment.getView() == null) {
                                    return;
                                }
                                int id = (messageDetails.startsWith("p1")) ? R.id.field_lightscreen : R.id.field_lightscreen_o;
                                battleFragment.getView().findViewById(id).setVisibility(View.INVISIBLE);
                            }
                        });
                        break;

                    case "auroraveil":
                        side = Character.toUpperCase(side.charAt(0)) + side.substring(1);
                        toAppendBuilder.append(side).append("的极光幕消失了！!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                if (battleFragment.getView() == null) {
                                    return;
                                }
                                int id = (messageDetails.startsWith("p1")) ? R.id.field_auroraveil : R.id.field_auroraveil_o;
                                battleFragment.getView().findViewById(id).setVisibility(View.INVISIBLE);
                            }
                        });
                        break;

                    case "safeguard":
                        side = Character.toUpperCase(side.charAt(0)) + side.substring(1);
                        toAppendBuilder.append(side).append("不再受神秘守护的保护!");
                        break;

                    case "mist":
                        side = Character.toUpperCase(side.charAt(0)) + side.substring(1);
                        toAppendBuilder.append(side).append(" 不再受迷雾的保护!");
                        break;

                    case "luckychant":
                        side = Character.toUpperCase(side.charAt(0)) + side.substring(1);
                        toAppendBuilder.append(side).append("的幸运咒语结束了!");
                        break;

                    case "firepledge":
                        toAppendBuilder.append("The sea of fire around ").append(side).append(" disappeared!");
                        break;

                    case "waterpledge":
                        toAppendBuilder.append("The rainbow on ").append(side).append("'s side disappeared!");
                        break;

                    case "grasspledge":
                        toAppendBuilder.append("The swamp around ").append(side).append(" disappeared!");
                        break;

                    default:
                        toAppendBuilder.append(battleFragment.getPrintable(fromEffect)).append(" ended!");
                        break;
                }

                logMessage = new SpannableString(toAppendBuilder);
                toast = battleFragment.makeMinorToast(logMessage);
                animatorSet.play(toast);
                battleFragment.startAnimation(animatorSet, message);
                break;

            case "-weather":
                String get;
                if (split[0].contains("ability")) {
                    get = split[1].substring(0, split[1].indexOf("|"));
                } else {
                    get = split[0];
                }


                boolean upkeep = false;
                if (split.length > 1 && split[1].contains("upkeep")) {
                    upkeep = true;
                }

                final String weather = get;

                animatorSet = new AnimatorSet();
                switch (weather) {
                    case "RainDance":
                        if (upkeep) {
                            toAppendBuilder.append("雨一直下!");
                        } else {
                            toAppendBuilder.append("开始下雨了!");
                            battleFragment.setWeatherExist(true);
                            animatorSet.addListener(new AnimatorListenerWithNet() {
                                @Override
                                public void onAnimationStartWithNet(Animator animation) {
                                    if (battleFragment.getView() == null) {
                                        return;
                                    }
                                    ((ImageView) battleFragment.getView().findViewById(R.id.weather_background)).setImageResource(R.drawable.weather_raindance);
                                    ((TextView) battleFragment.getView().findViewById(R.id.weather)).setText(weather);
                                }
                            });
                        }
                        break;
                    case "PrimordialSea":
                        if (upkeep) {
                            toAppendBuilder.append("大雨丝毫没有停止!");
                        } else {
                            toAppendBuilder.append("开始下大雨了!");
                            battleFragment.setWeatherExist(true);
                            animatorSet.addListener(new AnimatorListenerWithNet() {
                                @Override
                                public void onAnimationStartWithNet(Animator animation) {
                                    if (battleFragment.getView() == null) {
                                        return;
                                    }
                                    ((ImageView) battleFragment.getView().findViewById(R.id.weather_background)).setImageResource(R.drawable.weather_raindance);
                                    ((TextView) battleFragment.getView().findViewById(R.id.weather)).setText(weather);
                                }
                            });
                        }
                        break;
                    case "Sandstorm":
                        if (upkeep) {
                            toAppendBuilder.append("沙尘暴还在继续.");
                        } else {
                            toAppendBuilder.append("刮起了沙尘暴!");
                            battleFragment.setWeatherExist(true);
                            animatorSet.addListener(new AnimatorListenerWithNet() {
                                @Override
                                public void onAnimationStartWithNet(Animator animation) {
                                    if (battleFragment.getView() == null) {
                                        return;
                                    }
                                    ((ImageView) battleFragment.getView().findViewById(R.id.weather_background)).setImageResource(R.drawable.weather_sandstorm);
                                    ((TextView) battleFragment.getView().findViewById(R.id.weather)).setText(weather);
                                }
                            });
                        }
                        break;
                    case "SunnyDay":
                        if (upkeep) {
                            toAppendBuilder.append("阳光很强!");
                        } else {
                            toAppendBuilder.append("阳光变强了!");
                            battleFragment.setWeatherExist(true);
                            animatorSet.addListener(new AnimatorListenerWithNet() {
                                @Override
                                public void onAnimationStartWithNet(Animator animation) {
                                    if (battleFragment.getView() == null) {
                                        return;
                                    }
                                    ((ImageView) battleFragment.getView().findViewById(R.id.weather_background)).setImageResource(R.drawable.weather_sunnyday);
                                    ((TextView) battleFragment.getView().findViewById(R.id.weather)).setText(weather);
                                }
                            });
                        }
                        break;
                    case "DesolateLand":
                        if (upkeep) {
                            toAppendBuilder.append("炽热的阳光丝毫没有消退!");
                        } else {
                            toAppendBuilder.append("阳光变得异常刺眼了！");
                            battleFragment.setWeatherExist(true);
                            animatorSet.addListener(new AnimatorListenerWithNet() {
                                @Override
                                public void onAnimationStartWithNet(Animator animation) {
                                    if (battleFragment.getView() == null) {
                                        return;
                                    }
                                    ((ImageView) battleFragment.getView().findViewById(R.id.weather_background)).setImageResource(R.drawable.weather_sunnyday);
                                    ((TextView) battleFragment.getView().findViewById(R.id.weather)).setText(weather);
                                }
                            });
                        }
                        break;
                    case "Hail":
                        if (upkeep) {
                            toAppendBuilder.append("冰雹还在持续！");
                        } else {
                            toAppendBuilder.append("开始下冰雹了!");
                            battleFragment.setWeatherExist(true);
                            animatorSet.addListener(new AnimatorListenerWithNet() {
                                @Override
                                public void onAnimationStartWithNet(Animator animation) {
                                    if (battleFragment.getView() == null) {
                                        return;
                                    }
                                    ((ImageView) battleFragment.getView().findViewById(R.id.weather_background)).setImageResource(R.drawable.weather_hail);
                                    ((TextView) battleFragment.getView().findViewById(R.id.weather)).setText(weather);
                                }
                            });
                        }
                        break;
                    case "DeltaStream":
                        if (upkeep) {
                            toAppendBuilder.append("神秘的气流仍然在持续着!");
                        } else {
                            toAppendBuilder.append("神秘的乱气流产生了!");
                            battleFragment.setWeatherExist(true);
                            animatorSet.addListener(new AnimatorListenerWithNet() {
                                @Override
                                public void onAnimationStartWithNet(Animator animation) {
                                    if (battleFragment.getView() == null) {
                                        return;
                                    }
                                    ((ImageView) battleFragment.getView().findViewById(R.id.weather_background)).setImageResource(R.drawable.weather_strongwind);
                                    ((TextView) battleFragment.getView().findViewById(R.id.weather)).setText(weather);
                                }
                            });
                        }
                        break;
                    case "none":
                        if (battleFragment.isWeatherExist()) {
                            switch (battleFragment.getCurrentWeather()) {
                                case "RainDance":
                                    toAppendBuilder.append("雨停了.");
                                    break;
                                case "PrimordialSea":
                                    toAppendBuilder.append("倾盆大雨不下了!");
                                    break;
                                case "SunnyDay":
                                    toAppendBuilder.append("阳光减弱了.");
                                    break;
                                case "DesolateLand":
                                    toAppendBuilder.append("刺眼的阳光减弱了.");
                                    break;
                                case "Sandstorm":
                                    toAppendBuilder.append("沙暴停止了.");
                                    break;
                                case "Hail":
                                    toAppendBuilder.append("冰雹停止了.");
                                    break;
                                case "DeltaStream":
                                    toAppendBuilder.append("神秘的乱气流消散了.");
                            }
                            animatorSet.addListener(new AnimatorListenerWithNet() {
                                @Override
                                public void onAnimationStartWithNet(Animator animation) {
                                    if (battleFragment.getView() == null) {
                                        return;
                                    }
                                    ((ImageView) battleFragment.getView().findViewById(R.id.weather_background)).setImageResource(0);
                                    ((TextView) battleFragment.getView().findViewById(R.id.weather)).setText(null);
                                }
                            });
                        }
                        battleFragment.setWeatherExist(false);
                        break;
                }
                battleFragment.setCurrentWeather(weather);
                logMessage = new SpannableString(toAppendBuilder);
                toast = battleFragment.makeMinorToast(logMessage);
                animatorSet.play(toast);
                battleFragment.startAnimation(animatorSet, message);
                break;

            case "-fieldstart":
                attackerOutputName = translatePokemon(ofSource);
                animatorSet = new AnimatorSet();
                switch (MyApplication.toId(battleFragment.getPrintable(split[0]))) {
                    case "trickroom":
                        toAppendBuilder.append(attackerOutputName).append("扭曲了时空!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                if (battleFragment.getView() == null) {
                                    return;
                                }
                                ((ImageView) battleFragment.getView().findViewById(R.id.battle_background)).setImageResource(R.drawable.weather_trickroom);
                            }
                        });
                        break;

                    case "wonderroom":
                        toAppendBuilder.append("制造出了防御与特防能力值互换的奇妙空间!");
                        break;

                    case "magicroom":
                        toAppendBuilder.append("制造出了道具无效的魔法空间!");
                        break;

                    case "gravity":
                        toAppendBuilder.append("重力增强了!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                if (battleFragment.getView() == null) {
                                    return;
                                }
                                ((TextView) battleFragment.getView().findViewById(R.id.gra_vity)).setText("Gravity");
                            }
                        });
                        break;

                    case "mudsport":
                        toAppendBuilder.append("电系的威力减弱了!");
                        break;

                    case "watersport":
                        toAppendBuilder.append("火焰的威力减弱了!");
                        break;

                    case "electricterrain":
                        toAppendBuilder.append("脚下电光飞闪!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                if (battleFragment.getView() == null) {
                                    return;
                                }
                                ((ImageView) battleFragment.getView().findViewById(R.id.battle_terrain)).setImageResource(R.drawable.terrain_electric);
                            }
                        });
                        break;

                    case "psychicterrain":
                        toAppendBuilder.append("脚下传来了奇妙的感觉!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                if (battleFragment.getView() == null) {
                                    return;
                                }
                                ((ImageView) battleFragment.getView().findViewById(R.id.battle_terrain)).setImageResource(R.drawable.terrain_psychic);
                            }
                        });
                        break;

                    case "grassyterrain":
                        toAppendBuilder.append("脚下绿草如茵!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                if (battleFragment.getView() == null) {
                                    return;
                                }
                                ((ImageView) battleFragment.getView().findViewById(R.id.battle_terrain)).setImageResource(R.drawable.terrain_grassy);
                            }
                        });
                        break;

                    case "mistyterrain":
                        toAppendBuilder.append("脚下雾气缭绕!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                if (battleFragment.getView() == null) {
                                    return;
                                }
                                ((ImageView) battleFragment.getView().findViewById(R.id.battle_terrain)).setImageResource(R.drawable.terrain_misty);
                            }
                        });
                        break;

                    default:
                        toAppendBuilder.append(battleFragment.getPrintable(split[0])).append(" started!");
                        break;
                }
                logMessage = new SpannableString(toAppendBuilder);
                toast = battleFragment.makeMinorToast(logMessage);
                animatorSet.play(toast);
                battleFragment.startAnimation(animatorSet, message);
                break;

            case "-fieldend":
                animatorSet = new AnimatorSet();
                switch (MyApplication.toId(battleFragment.getPrintable(split[0]))) {
                    case "trickroom":
                        toAppendBuilder.append("扭曲的时空复原了!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                if (battleFragment.getView() == null) {
                                    return;
                                }
                                int id = new Random().nextInt(BattleFragment.BACKGROUND_LIBRARY.length);
                                ((ImageView) battleFragment.getView().findViewById(R.id.battle_background)).setImageResource(BattleFragment.BACKGROUND_LIBRARY[id]);
                            }
                        });
                        break;

                    case "wonderroom":
                        toAppendBuilder.append("奇妙空间结束了,防御与特防能力值恢复了正常!");
                        break;

                    case "magicroom":
                        toAppendBuilder.append("魔法空间结束了,道具效果恢复了正常!!");
                        break;

                    case "gravity":
                        toAppendBuilder.append("重力复原了!");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                if (battleFragment.getView() == null) {
                                    return;
                                }
                                ((TextView) battleFragment.getView().findViewById(R.id.gra_vity)).setText(null);
                            }
                        });
                        break;

                    case "mudsport":
                        toAppendBuilder.append("玩泥的效果结束了.");
                        break;

                    case "watersport":
                        toAppendBuilder.append("玩水的效果结束了.");
                        break;

                    case "electricterrain":
                        toAppendBuilder.append("The battlefield returned to normal.");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                if (battleFragment.getView() == null) {
                                    return;
                                }
                                ((ImageView) battleFragment.getView().findViewById(R.id.battle_terrain)).setImageResource(0);
                            }
                        });
                        break;

                    case "psychicterrain":
                        toAppendBuilder.append("脚下奇妙的感觉消失了.");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                if (battleFragment.getView() == null) {
                                    return;
                                }
                                ((ImageView) battleFragment.getView().findViewById(R.id.battle_terrain)).setImageResource(0);
                            }
                        });
                        break;

                    case "grassyterrain":
                        toAppendBuilder.append("脚下的青草不见了.");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                if (battleFragment.getView() == null) {
                                    return;
                                }
                                ((ImageView) battleFragment.getView().findViewById(R.id.battle_terrain)).setImageResource(0);
                            }
                        });
                        break;

                    case "mistyterrain":
                        toAppendBuilder.append("脚下的雾气不见了.");
                        animatorSet.addListener(new AnimatorListenerWithNet() {
                            @Override
                            public void onAnimationStartWithNet(Animator animation) {
                                if (battleFragment.getView() == null) {
                                    return;
                                }
                                ((ImageView) battleFragment.getView().findViewById(R.id.battle_terrain)).setImageResource(0);
                            }
                        });
                        break;

                    default:
                        toAppendBuilder.append(battleFragment.getPrintable(split[0])).append(" ended!");
                        break;
                }
                logMessage = new SpannableString(toAppendBuilder);
                toast = battleFragment.makeMinorToast(logMessage);
                animatorSet.play(toast);
                battleFragment.startAnimation(animatorSet, message);
                break;

            case "-fieldactivate":
                switch (MyApplication.toId(battleFragment.getPrintable(split[0]))) {
                    case "perishsong":
                        toAppendBuilder.append("所有听到歌声的宝可梦将会在3回合后灭亡!");
                        break;

                    case "payday":
                        toAppendBuilder.append("Coins were scattered everywhere!");
                        break;

                    case "iondeluge":
                        toAppendBuilder.append("A deluge of ions showers the battlefield!");
                        break;

                    default:
                        toAppendBuilder.append(battleFragment.getPrintable(split[1])).append(" hit!");
                        break;
                }
                logMessage = new SpannableString(toAppendBuilder);
                toast = battleFragment.makeMinorToast(logMessage);
                battleFragment.startAnimation(toast, message);
                break;

            case "-message":
            case "-hint":
                logMessage = new SpannableString(messageDetails);
                toast = battleFragment.makeMinorToast(logMessage);
                battleFragment.startAnimation(toast, message);
                break;

            case "-anim":
                logMessage = new SpannableString(command + ":" + messageDetails);
                toast = battleFragment.makeMinorToast(logMessage);
                battleFragment.startAnimation(toast, message);
                break;

            default:
                toAppendSpannable = new SpannableString(command + ":" + messageDetails);
                toast = battleFragment.makeMinorToast(toAppendSpannable);
                battleFragment.startAnimation(toast, message);
                logMessage = new SpannableString(command + ":" + messageDetails);
                break;
        }

        if (messageDetails.contains("[silent]")) {
            return;
        }

        logMessage.setSpan(new RelativeSizeSpan(0.8f), 0, logMessage.toString().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        battleFragment.addToLog(logMessage);
    }
}
