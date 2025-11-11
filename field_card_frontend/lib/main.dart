import 'package:field_card_frontend/theme/app_colors.dart';
import 'package:flutter/material.dart';

import 'screens/vademecum_search_screen.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      theme: ThemeData(
          useMaterial3: true,
          brightness: Brightness.light,
          colorScheme: ColorScheme.fromSeed(
            seedColor: primaryColor,
            primary: primaryColor,
            brightness: Brightness.light,
          )),
      darkTheme: ThemeData(
          useMaterial3: true,
          brightness: Brightness.dark,
          colorScheme: ColorScheme.fromSeed(
            seedColor: primaryColor,
            primary: primaryColor,
            brightness: Brightness.dark,
          )),
      themeMode: ThemeMode.system,
      home: VademecumSearchScreen(),
    );
  }
}
