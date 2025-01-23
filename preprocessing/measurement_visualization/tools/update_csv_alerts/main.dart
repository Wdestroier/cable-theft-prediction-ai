import 'dart:io';

const inputPath = 'C:/Users/Wdest/Desktop/filtered_tb_feeds.csv';
const outputPath = 'C:/Users/Wdest/Desktop/categorized_feeds.csv';

void main() {
  if (File(outputPath).existsSync()) {
    File(outputPath).deleteSync();
  }

  // Define multiple date ranges in which you want to set alert=1.
  final dateRanges = <(DateTime start, DateTime end)>[
    (
      DateTime.parse('2024-11-28T10:40:00Z'),
      DateTime.parse('2024-11-28T22:59:00Z')
    ),
    (
      DateTime.parse('2024-11-29T10:43:00Z'),
      DateTime.parse('2024-11-29T17:13:00Z')
    ),
    (
      DateTime.parse('2024-11-29T20:19:00Z'),
      DateTime.parse('2024-11-29T23:14:00Z')
    ),
    (
      DateTime.parse('2024-12-01T18:02:00Z'),
      DateTime.parse('2024-12-01T19:20:00Z')
    ),
    (
      DateTime.parse('2024-12-02T10:00:00Z'),
      DateTime.parse('2024-12-03T01:30:00Z')
    ),
    (
      DateTime.parse('2024-12-03T10:45:00Z'),
      DateTime.parse('2024-12-03T22:27:00Z')
    ),
    (
      DateTime.parse('2024-12-04T10:34:00Z'),
      DateTime.parse('2024-12-04T23:46:00Z')
    ),
    (
      DateTime.parse('2024-12-05T10:30:00Z'),
      DateTime.parse('2024-12-05T23:03:00Z')
    ),
    (
      DateTime.parse('2024-12-06T10:38:00Z'),
      DateTime.parse('2024-12-06T21:48:00Z')
    ),
    (
      DateTime.parse('2024-12-06T23:43:00Z'),
      DateTime.parse('2024-12-10T21:29:00Z')
    ),
    (
      DateTime.parse('2024-12-11T10:36:00Z'),
      DateTime.parse('2024-12-11T22:11:00Z')
    ),
    (
      DateTime.parse('2024-12-12T10:06:00Z'),
      DateTime.parse('2024-12-12T23:38:00Z')
    ),
    (
      DateTime.parse('2024-12-13T09:41:00Z'),
      DateTime.parse('2024-12-13T21:29:00Z')
    ),
    (
      DateTime.parse('2024-12-14T11:17:00Z'),
      DateTime.parse('2024-12-14T20:59:00Z')
    ),
  ];

  modifyRanges(
    inputPath: inputPath,
    outputPath: outputPath,
    dateRanges: dateRanges,
  );
}

/// Reads each line from [inputPath], ensures there's an alert column,
/// and sets alert="1" if the row's timestamp falls within *any* of the
/// [dateRanges]. Writes the updated lines to [outputPath].
void modifyRanges({
  required String inputPath,
  required String outputPath,
  required List<(DateTime start, DateTime end)> dateRanges,
}) {
  final lines = File(inputPath).readAsLinesSync();
  final updatedLines = <String>[];

  for (var line in lines) {
    // Skip empty lines
    if (line.trim().isEmpty) {
      updatedLines.add(line);
      continue;
    }

    final columns = line.split(',');

    // If this row doesn't have 6 columns yet, assume alert is missing => add it as "0"
    // Columns: [0] device_id, [1] created_at, [2] feed_entry_id,
    //          [3] lum, [4] vib, [5] alert
    if (columns.length < 6) {
      columns.add('"0"');
    }

    // Attempt to parse the `created_at` column (index 1)
    // which might look like: "2024-11-26T20:51:07Z"
    try {
      final dateStr = columns[1].replaceAll('"', '');
      final dateTime = DateTime.parse(dateStr);

      // Check if the timestamp is in *any* of the provided dateRanges
      for (final range in dateRanges) {
        final (start, end) = range;

        if (dateTime.isAfter(start) && dateTime.isBefore(end)) {
          columns[5] = '"1"';
          // Once we've set alert=1, no need to check more ranges
          break;
        }
      }
    } catch (_) {
      // If parsing fails, do nothing (date remains unmodified, alert stays the same).
    }

    updatedLines.add(columns.join(','));
  }

  // Write out the entire updated CSV
  File(outputPath).writeAsStringSync(updatedLines.join('\n'));
}
