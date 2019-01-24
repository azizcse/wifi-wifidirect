package com.w3engineers.meshrnd;

/*
 *  ****************************************************************************
 *  * Created by : Md. Azizul Islam on 1/14/2019 at 5:06 PM.
 *  * Email : azizul@w3engineers.com
 *  *
 *  * Purpose:
 *  *
 *  * Last edited by : Md. Azizul Islam on 1/14/2019.
 *  *
 *  * Last Reviewed by : <Reviewer Name> on <mm/dd/yy>
 *  ****************************************************************************
 */


import com.w3.meshlib.MeshApp;
import com.w3engineers.meshrnd.util.SharedPref;

public class App extends MeshApp {

    @Override
    public void onCreate() {
        super.onCreate();

        SharedPref.on(this);
    }


}
