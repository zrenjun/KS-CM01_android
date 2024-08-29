/**
* 这个地方能收到数据
*/
* 
* 
```kotlin

     override fun onNewData(data: ByteArray?) {
        data?.apply {
            pool = com.example.pl2302_android.uart.add(pool, this)
        }
        pool?.apply {
            pool = handleDataPool(pool)
        }
    }


```
