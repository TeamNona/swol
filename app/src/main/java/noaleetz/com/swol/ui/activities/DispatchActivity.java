package noaleetz.com.swol.ui.activities;

import com.parse.ui.login.ParseLoginDispatchActivity;

public class DispatchActivity extends ParseLoginDispatchActivity {

    @Override
    protected Class<?> getTargetClass() {
        return MainActivity.class;
    }

}
