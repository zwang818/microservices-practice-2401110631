-- 应用第一次启动建表后执行。使用稳定编号查找数据，重复执行不会增加相同样例。
START TRANSACTION;

INSERT INTO equipment (code, name, category, daily_rate, deposit, status, description, created_at)
SELECT 'DEMO-CAM-001', '课程演示相机', '摄影设备', 120.00, 1000.00, 'AVAILABLE', '任务三持久化样例', CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (SELECT 1 FROM equipment WHERE code = 'DEMO-CAM-001');

INSERT INTO reservation (equipment_id, renter_name, start_time, end_time, status, created_at)
SELECT e.id, '演示用户', '2026-10-20 09:00:00', '2026-10-21 09:00:00', 'CONFIRMED', CURRENT_TIMESTAMP(6)
FROM equipment e
WHERE e.code = 'DEMO-CAM-001'
  AND NOT EXISTS (
      SELECT 1 FROM reservation r
      WHERE r.equipment_id = e.id AND r.status = 'CONFIRMED'
        AND r.start_time < '2026-10-21 09:00:00' AND r.end_time > '2026-10-20 09:00:00'
  );

COMMIT;
