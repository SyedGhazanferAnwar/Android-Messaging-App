package com.example.ghazanfer.messenger_fin;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendProfile extends Fragment {

    TextView username_top;
    TextView t_name;
    public static String name;
    public static String profile_name;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_friend_profile, container, false);
        username_top= (TextView) view.findViewById(R.id.username_top);
        username_top.setText(profile_name);
        t_name= (TextView) view.findViewById(R.id.t_name);
        t_name.setText(name);
        hideKeyboard(getActivity());
        return view;
    }
    public void updateinfo(){
        t_name.setText(profile_name);
    }
    public static void hideKeyboard(Activity activity) {
        InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);

        // check if no view has focus:
        View currentFocusedView = activity.getCurrentFocus();
        if (currentFocusedView != null) {
            inputManager.hideSoftInputFromWindow(currentFocusedView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}
