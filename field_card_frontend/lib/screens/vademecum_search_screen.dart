import 'package:flutter/material.dart';

import '../widgets/primary_button.dart';
import '../widgets/search_field.dart';

class VademecumSearchScreen extends StatefulWidget {
  const VademecumSearchScreen({Key? key}) : super(key: key);

  @override
  State<VademecumSearchScreen> createState() => _VademecumSearchScreenState();
}

class _VademecumSearchScreenState extends State<VademecumSearchScreen> {
  final TextEditingController _controller = TextEditingController();

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              SearchField(
                hintText: 'Wyszukaj',
                onChanged: (value) {},
              ),
              PrimaryButton(
                label: 'Szukaj',
                onPressed: () {},
              ),
            ],
          ),
        ),
      ),
    );
  }
}
