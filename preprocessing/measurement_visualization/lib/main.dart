import 'dart:io';
import 'package:flutter/material.dart';
import 'package:syncfusion_flutter_charts/charts.dart';
import 'package:intl/intl.dart';

import 'csv_data.dart' show csvData;

class CSVReading {
  final DateTime date;
  final double lum;
  final double vib;
  final int alert;

  CSVReading({
    required this.date,
    required this.lum,
    required this.vib,
    required this.alert,
  });

  @override
  String toString() =>
      'CSVReading(date: $date, lum: $lum, vib: $vib, alert: $alert)';
}

void main() {
  runApp(const MeasurementVisualizationApp());
}

class MeasurementVisualizationApp extends StatefulWidget {
  const MeasurementVisualizationApp({super.key});

  @override
  State<MeasurementVisualizationApp> createState() =>
      _MeasurementVisualizationAppState();
}

class _MeasurementVisualizationAppState
    extends State<MeasurementVisualizationApp> {
  late List<CSVReading> _parsedRows;
  String? _errorMessage;

  @override
  void initState() {
    super.initState();
    _parseCSV();
  }

  void _parseCSV() {
    try {
      final lines = csvData.split('\n');
      final parsed = <CSVReading>[];

      for (int i = 0; i < lines.length; i++) {
        final line = lines[i].trim();
        if (line.isEmpty) continue;

        final columns = line.split(',');
        // We now expect 6 columns:
        // [0: device_id, 1: created_at, 2: feed_entry_id, 3: lum, 4: vib, 5: alert]
        if (columns.length < 6) continue;

        // Remove quotes
        for (int c = 0; c < columns.length; c++) {
          columns[c] = columns[c].replaceAll('"', '').trim();
        }

        // Skip the header if it starts with 'device_id'
        if (i == 0 && columns[0].toLowerCase().contains('device_id')) {
          continue;
        }

        // Parse date/time
        late DateTime dt;
        try {
          dt = DateTime.parse(columns[1]);
        } catch (_) {
          continue; // invalid date
        }

        // Parse luminosity
        late double lumVal;
        try {
          lumVal = double.parse(columns[3]);
        } catch (_) {
          continue; // invalid lum
        }

        // Parse vibration
        late double vibVal;
        try {
          vibVal = double.parse(columns[4]);
        } catch (_) {
          continue; // invalid vib
        }

        // Parse alert (0 or 1)
        late int alertVal;
        try {
          alertVal = int.parse(columns[5]);
        } catch (_) {
          // default to 0 if parsing fails
          alertVal = 0;
        }

        parsed.add(CSVReading(
          date: dt,
          lum: lumVal,
          vib: vibVal,
          alert: alertVal,
        ));
      }

      // Sort by date
      parsed.sort((a, b) => a.date.compareTo(b.date));

      setState(() {
        _parsedRows = parsed;
        _errorMessage = null;
        if (_parsedRows.isNotEmpty) {
          print('First row: ${_parsedRows.first}');
          print('Last row: ${_parsedRows.last}');
        }
      });
    } catch (e) {
      setState(() {
        _parsedRows = [];
        _errorMessage = 'Parsing error: $e';
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Syncfusion CSV Chart Demo',
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Syncfusion CSV Chart Demo'),
        ),
        body: _errorMessage != null
            ? Center(child: Text(_errorMessage!))
            : _parsedRows.isEmpty
                ? const Center(child: Text('No valid data to display.'))
                : MeasurementChart(parsedRows: _parsedRows),
      ),
    );
  }
}

class MeasurementChart extends StatelessWidget {
  const MeasurementChart({super.key, required this.parsedRows});
  final List<CSVReading> parsedRows;

  @override
  Widget build(BuildContext context) {
    return SfCartesianChart(
      title: ChartTitle(text: 'Luminosity & Vibration (Colored by Alert)'),
      legend: Legend(isVisible: true),
      tooltipBehavior: TooltipBehavior(enable: true),
      zoomPanBehavior: ZoomPanBehavior(
        enablePinching: true,
        enablePanning: true,
        enableDoubleTapZooming: true,
        enableMouseWheelZooming: true,
      ),
      primaryXAxis: DateTimeAxis(
        dateFormat: DateFormat("yyyy-MM-dd HH:mm 'UTC'"),
        intervalType: DateTimeIntervalType.auto,
        edgeLabelPlacement: EdgeLabelPlacement.shift,
        majorGridLines: const MajorGridLines(width: 0),
      ),
      primaryYAxis: NumericAxis(
        majorGridLines: const MajorGridLines(width: 0),
      ),
      series: [
        // 1) Luminosity: color each point based on alert (0 or 1)
        LineSeries<CSVReading, DateTime>(
          name: 'Luminosity',
          dataSource: parsedRows,
          xValueMapper: (row, _) => row.date,
          yValueMapper: (row, _) => row.lum,
          pointColorMapper: (row, _) => _getLuminosityLineColor(row.alert),
        ),

        // 2) Vibration: color each point based on alert (0 or 1)
        LineSeries<CSVReading, DateTime>(
          name: 'Vibration',
          dataSource: parsedRows,
          xValueMapper: (row, _) => row.date,
          yValueMapper: (row, _) => row.vib,
          pointColorMapper: (row, _) => _getVibrationLineColor(row.alert),
        ),
      ],
    );
  }

  Color _getLuminosityLineColor(int alert) {
    return (alert == 0) ? Colors.grey[800]! : Colors.yellow;
  }

  Color _getVibrationLineColor(int alert) {
    return (alert == 0) ? Colors.grey[300]! : Colors.brown;
  }
}
