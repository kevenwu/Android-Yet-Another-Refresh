# Android-Yet-Another-Refresh

Library of effect of qzone like profile refresh.

## Demo

![demo](.screen.gif)

## Usage

### config in xml:

```xml
<com.kevenwu.refresh.lib.RefreshLayout
    android:id="@+id/refresh_layout"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <ListView
        android:id="@+id/list_view"
        android:layout_width="match_parent"
        android:layout_height="match_parent"/>

</com.kevenwu.refresh.lib.RefreshLayout>
```

### config in your code:

```java
RefreshLayout refreshLayout = (RefreshLayout)findViewById(R.id.refresh_layout);
View headerView = LayoutInflater.from(this).inflate(R.layout.list_header, null);
refreshLayout.setHeaderView(headerView, (ImageView)headerView.findViewById(R.id.fake), R.drawable.bg);
```

## Contributing
Please fork repository and contribute using pull requests.

## Credits
kevenwu

## License
Apache License Version 2.0

