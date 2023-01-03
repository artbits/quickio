## QuickIO Performance Reference Data
Data is for reference only. For specific test code, click [here](src/test/java/performance_test/).


```
QuickIO.DB test data.

Each data comes from the best of three consecutive limited test results.
100,000 pieces of data per operation.

CPU: Intel(R) Core(TM) i5-6200U CPU @ 2.30GHz   2.40 GHz
Memory: 8G
SSD: 256G
OS: Windows 10
JDK: 8
======================================================================================
[alone_save] time consumption: 940ms|0.940000s
[batch_save] time consumption: 766ms|0.766000s
--------------------------------------------------------------------------------------
[alone_update] time consumption: 847ms|0.847000s
[batch_update] time consumption: 2522ms|2.522000s
--------------------------------------------------------------------------------------
[along_delete] time consumption: 188ms|0.188000s
[batch_delete] time consumption: 125ms|0.125000s

[delete_all_by_class] time consumption: 1100ms|1.100000s
[delete_by_condition] time consumption: 1111ms|1.111000s
--------------------------------------------------------------------------------------
[find_first] time consumption: 937ms|0.937000s
[find_first_by_condition] time consumption: 1109ms|1.109000s

[find_last] time consumption: 921ms|0.921000s
[find_last_by_condition] time consumption: 892ms|0.892000s

[find_one_by_id] time consumption: 31ms|0.031000s
[find_one_by_condition] time consumption: 754ms|0.754000s

[find_all] time consumption: 1158ms|1.158000s
[find_all_by_ids] time consumption: 1111ms|1.111000s

[find_by_conditions] time consumption: 1094ms|1.094000s
[find_by_conditions_options_sort] time consumption: 1015ms|1.015000s
[find_by_conditions_options_sort_skip] time consumption: 1189ms|1.189000s
[find_by_conditions_options_sort_skip_limit] time consumption: 1120ms|1.120000s

[find_with_id] time consumption: 275ms|0.275000s
[find_with_id_options_sort] time consumption: 349ms|0.349000s
[find_with_id_options_sort_skip] time consumption: 426ms|0.426000s
[find_with_id_options_sort_skip_limit] time consumption: 343ms|0.343000s

[find_with_time] time consumption: 315ms|0.315000s
[find_with_time_options_sort] time consumption: 443ms|0.443000s
[find_with_time_options_sort_skip] time consumption: 487ms|0.487000s
[find_with_time_options_sort_skip_limit] time consumption: 500ms|0.500000s
```


```
QuickIO.KV test data.

Each data comes from the best of three consecutive limited test results.
100,000 pieces of data per operation.

CPU: Intel(R) Core(TM) i5-6200U CPU @ 2.30GHz   2.40 GHz
Memory: 8G
SSD: 256G
OS: Windows 10
JDK: 8
======================================================================================
[write_long_type_value] time consumption: 688ms|0.688000s
[write_string_type_value] time consumption: 728ms|0.728000s
[write_object_type_value] time consumption: 892ms|0.892000s
--------------------------------------------------------------------------------------
[read_long_type_value] time consumption: 282ms|0.282000s
[read_string_type_value] time consumption: 360ms|0.360000s
[read_object_type_value] time consumption: 156ms|0.156000s
--------------------------------------------------------------------------------------
[remove_key] time consumption: 296ms|0.296000s
[contains_key] time consumption: 268ms|0.268000s
```


```
QuickIO.Can test data.

Each data comes from the best of three consecutive limited test results.
The file size for each operation is 250MB.

CPU: Intel(R) Core(TM) i5-6200U CPU @ 2.30GHz   2.40 GHz
Memory: 8G
SSD: 256G
OS: Windows 10
JDK: 8
======================================================================================
[put_file] time consumption: 219ms|0.219000s
[get_file] time consumption: 125ms|0.125000s
[remove_file] time consumption: 62ms|0.062000s
```