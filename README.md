![Logo](art/logo.jpg)

# EventBus
Simple event bus for Android

### Samples
``` java
public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.create(this, true);
    }
}
```

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