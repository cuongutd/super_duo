package barqsoft.footballscores.service;

import android.content.Intent;
import android.widget.RemoteViewsService;

public class RemoteWidgetService extends RemoteViewsService {


    public RemoteWidgetService() {
    }


    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteWidgetFactory(this.getApplicationContext(), intent);
    }
}
