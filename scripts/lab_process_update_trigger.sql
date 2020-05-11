DROP TRIGGER IF EXISTS `lab_process_update_trigger`;

DELIMITER $$
 
create trigger lab_process_update_trigger
after update
on redcap_data for each row
begin
if (new.project_id = 362) then
	if (new.field_name = 'lab_reception_complete' and new.value = '2') then
		CALL labReception(new.project_id,
							new.event_id,
							new.record,
							new.field_name,
							new.value,
							new.instance);
	elseif (new.field_name = 'lab_reception_condition_complete' and new.value = '2') then
		CALL labReceptionCondition(new.project_id,
									new.event_id,
									new.record,
									new.field_name,
									new.value,
									new.instance);
	elseif (new.field_name = 'testing_reception_complete' and new.value = '2') then
		CALL labTestingPointOfReception(new.project_id,
										new.event_id,
										new.record,
										new.field_name,
										new.value,
										new.instance);
	end if;

elseif (new.project_id = 345) then
	if (new.field_name = 'testing_detection_complete' and new.value = '2') then
		CALL labTestingDetection(new.project_id,
										new.event_id,
										new.record,
										new.field_name,
										new.value,
										new.instance);
	elseif (new.field_name = 'resulting_complete' and new.value = '2') then
		CALL labTestingResulting(new.project_id,
										new.event_id,
										new.record,
										new.field_name,
										new.value,
										new.instance);
	elseif (new.field_name = 'verification_complete' and new.value = '2') then
		CALL labTestingVerification(new.project_id,
										new.event_id,
										new.record,
										new.field_name,
										new.value,
										new.instance);
	end if;
end if;
end; $$

DELIMITER ;
