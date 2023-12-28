# CanvasToVideo

Android で Canvas から動画を作る。

https://user-images.githubusercontent.com/32033405/235225944-748106f4-89a0-48c0-b14d-7edfd2698b96.mp4

https://user-images.githubusercontent.com/32033405/235321119-e8391d46-9fbd-4ed2-9cc7-5a2dd72b7d10.mp4

## Android 14 からは AV1 エンコーダーが使えるみたいです（？）
エンコーダーに渡すパラメーターを`AV1`にすれば良いはず。  

https://github.com/takusan23/CanvasToVideo/assets/32033405/0ac1c790-d91e-4333-9b72-aeefb8bd2d41

https://github.com/takusan23/CanvasToVideo/assets/32033405/bf049fa0-7ba8-4857-a90a-311c7b3cc977

# 開発者向け

## 利用ライブラリ
以下のコードをパクってきています。参考にする際は↓からライブラリを追加するといいと思います。  

https://github.com/takusan23/AkariDroid/tree/master/akari-core

## 見ると良いコード

- app/src/main/java/io/github/takusan23/canvastovideo/BasicScreen.kt
  - ただ再生時間を文字にして動画を作成する
- app/src/main/java/io/github/takusan23/canvastovideo/SlideShowScreen.kt
  - 選択した画像を繋げた動画を作成する
- app/src/main/java/io/github/takusan23/canvastovideo/EndRollScreen.kt
  - エンドロールみたいな動画を作成する
