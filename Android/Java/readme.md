# Start.io Mediation Adapter for IronSource (Android)

## Setup Start.io

1. [Open a Publisher account][1]
2. [Add an App][2]
3. Make sure you find an **Account ID** and **App ID**
![Account Id, App ID](images/01.png)

## Setup IronSource

**IMPORTANT**: Because IronSource Custom Adapters is still in beta-testing phase,
you should [contact IronSource support][5] and ask them to enable Custom Adapters for your app.

4. Register **Start.Io** [custom adapter][3] for your IronSource account

![Custom Adapter](images/02.png)

Network key: **15b99c96d**

Field **Ad Tag** is mandatory, but if you don't want to use it, you can set value **default**.

![Interstitial setup](images/03.png)

## Setup project

5. Add dependency on Start.Io IronSource Mediation library

```
dependencies {
    // noinspection GradleDependency
    implementation 'com.startapp:ironsource-mediation:1.+'
}
```

## Testing

6. Use your IronSource **App Key** with this demo project. Put your value on [this line][4] then compile and launch.

![Test with your App Key](images/04.png)

 [1]: https://support.start.io/hc/en-us/articles/202766673
 [2]: https://support.start.io/hc/en-us/articles/202766743
 [3]: https://developers.is.com/ironsource-mobile/general/custom-adapter-setup
 [4]: https://github.com/StartApp-SDK/ironsource-mediation/blob/7f9fd526375f8f6a3da6fd2945c81460a7b36cd9/Android/Java/app/src/main/java/com/ironsource/ironsourcesdkdemo/DemoActivity.java#L37
 [5]: https://ironsrc.formtitan.com/contact-us
