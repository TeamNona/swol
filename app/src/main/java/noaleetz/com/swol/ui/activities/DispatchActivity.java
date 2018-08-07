package noaleetz.com.swol.ui.activities;

import android.app.ActionBar;
        import android.os.Bundle;
        import android.os.PersistableBundle;
        import android.support.annotation.Nullable;
        import android.widget.Toolbar;

        import com.parse.ui.login.ParseLoginDispatchActivity;

public class DispatchActivity extends ParseLoginDispatchActivity {



    @Override
    protected Class<?> getTargetClass() {
        return MainActivity.class;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);

        ActionBar actionBar = getActionBar();
        actionBar.hide();
    }


}
