# appbar-snap-behavior
The current implementation of the AppBarLayout in `com.android.support:design:23.1.1` has the SNAP flag, but it doesn't work as the GooglePlay app: when it's snapping, even the scrolling container is moving.
Applying the behaviors contained into this library to the AppBarLayout and to the scrolling container, it's possibile to have a snapping AppBar that is moving independently from the scrolling container, achieving a result very similar to the one showed in the GooglePlay app.

## Installation
``` groovy
compile "com.github.godness84:appbar-snap-behavior:0.1.4"
```

`appbar-snap-behavior` is deployed to [jitpack.io](https://jitpack.io/). Make sure you have `maven { url "https://jitpack.io" }` in your root build.gradle at the end of repositories:
``` groovy
allprojects {
		repositories {
			...
			maven { url "https://jitpack.io" }
		}
}
```


## Usage
Just 2 steps:
- Add `app:layout_behavior="com.github.godness84.appbarsnapbehavior.AppBarSnapBehavior"` to your AppBarLayout.
- Use `app:layout_behavior="com.github.godness84.appbarsnapbehavior.ScrollingViewBehavior"` in your scrolling container (eg. ViewPager, NestedScrollView or RecyclerView).
