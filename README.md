![Logo](art/logo.jpg)

# EventBus
Simple event bus for Android

### Latest version
[ ![Download](https://api.bintray.com/packages/nickandroid/maven/eventbus/images/download.svg) ](https://bintray.com/nickandroid/maven/eventbus/_latestVersion)

### Samples

Config
``` java
public class MyApp extends Application {
    
    @Override
    public void onCreate() {
        super.onCreate();
        Scalpel.create(this).config(Configuration.builder()
                .debug(true)
                .logTag("EventBus")
                .build());
        EventBus.create(this).setDebuggable(true);
    }
}
```

Activity
``` java
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EventBus.getInstance().publishEmpty(Constants.EVENT_FAB_CLICKED);
            }
        });

        Intent intent = new Intent(this, MyService.class);
        startService(intent);

        EventBus.getInstance().subscribe(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getInstance().publishEmpty(Constants.EVENT_ACTIVITY_FINISHED);
    }

    @CallInMainThread
    @Events(Constants.EVENT_LOG)
    public void handleLog(Event event) {
        TextView textView = (TextView) findViewById(R.id.textView);
        textView.setText(event.getData().getString("data"));
    }
```

Service
``` java
@Events(Constants.EVENT_ACTIVITY_FINISHED)
public class MyService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        log("Service started.");
        EventBus.getInstance().subscribe(this);
    }

    @Events(Constants.EVENT_FAB_CLICKED)
    public void handleFabClick() {
        log("handleFabClick");
    }

    @Events(Constants.EVENT_FAB_CLICKED)
    public void handleFabClickWithParam(Event event) {
        log("handleFabClickWithParam:" + event);
    }

    @ReceiverMethod
    @Events({Constants.EVENT_FAB_CLICKED, Constants.EVENT_ACTIVITY_FINISHED})
    public void customName() {
        log("customName");
    }

    @ReceiverMethod
    public void customNameWithParam(Event event) {
        log("customNameWithParam:" + event);
    }

    void log(Object msg) {
        Thread calling = Thread.currentThread();
        Log.d("EventBusSample", String.valueOf(msg) + ", calling in thread:" + calling.getName());

        Bundle data = new Bundle();
        data.putString("data", String.valueOf(msg));
        Event event = new Event(Constants.EVENT_LOG, data);

        EventBus.getInstance().publish(event);
    }
}
```

Remote call
``` java

public class EventBusServiceSample extends EventBusService {
}
```
``` java
public class Binding {
    IEventBus mBus;

    public Binding(IEventBus bus) {
        this.mBus = bus;
    }

    void bind() {
        try {
            mBus.subscribe(new IEventReceiver.Stub() {
                @Override
                public void onReceive(Event event) throws RemoteException {
                    Log.d("EventBus.Binding", "onReceive:" + event);
                }

                @Override
                public int[] events() throws RemoteException {
                    return new int[]{Constants.EVENT_FAB_CLICKED};
                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
```
