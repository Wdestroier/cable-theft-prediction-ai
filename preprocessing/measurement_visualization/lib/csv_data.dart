import 'dart:io';

final String csvData = File(r'C:\Users\Wdest\Desktop\feeds.csv')
    .readAsLinesSync()
    .reversed
    .take(Duration(days: 30).inMinutes)
    .join('\n');
