package noaleetz.com.swol;

import com.parse.ui.login.ParseLoginDispatchActivity;

public class DispatchActivity extends ParseLoginDispatchActivity {

    @Override
    protected Class<?> getTargetClass() {
        return MainActivity.class;
    }
}
