DROP TRIGGER IF EXISTS `lab_testing_detection_complete_update_trigger`;

DELIMITER $$
 
create trigger lab_testing_detection_complete_update_trigger
after update
on redcap_data for each row
begin

	declare lab_report smallint(4);
	declare lab_receiving smallint(4);
	declare lab_extraction smallint(4);
	declare lab_results smallint(4);
    declare count_ int default 1;
    
	declare report_max_event smallint(4);
	declare detection_lab varchar(255);
	declare test_det_datetime varchar(255);
	declare test_det_personnel varchar(255);
	declare test_det_instrument varchar(255);
	declare test_det_batchsize int;
	declare test_det_batch_id varchar(255);
	declare test_det_barcode varchar(255);
	declare pos varchar(2);
	DECLARE done INT DEFAULT FALSE;
	
	declare c_project_id int(10);
	declare c_event_id int(10);
	declare c_record varchar(100);
	declare c_field_name varchar(100);
	declare c_value text;
	declare c_instance smallint(4);
    
	declare specimen cursor for
	select project_id, event_id, record, field_name, value, instance
	from redcap_data
	where project_id = new.project_id
		and event_id = new.event_id
		and record = new.record
		and field_name like 'test_det_barcode_%';
    
	DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

	select 352 into lab_report;	
	select 362 into lab_receiving;	
	select 344 into lab_extraction;	
	select 345 into lab_results;
	
	-- Stop calling this procedure with any other data
	if (new.field_name = 'testing_detection_complete' and new.value = '2' and new.project_id = lab_results) then
		
		select max(event_id) into report_max_event from redcap_data where project_id = lab_report;
        
        insert into my_log (dt, what) values (now(), '1');
		
		select value into detection_lab
		from redcap_data 
		where project_id = new.project_id 
			and event_id = new.event_id 
			and record = new.record 
			and field_name = 'detection_lab';
            			
		select value into test_det_datetime
		from redcap_data 
		where project_id = new.project_id 
			and event_id = new.event_id 
			and record = new.record 
			and field_name = 'test_det_datetime';
            			
		select value into test_det_personnel
		from redcap_data 
		where project_id = new.project_id 
			and event_id = new.event_id 
			and record = new.record 
			and field_name = 'test_det_personnel';
		
		select value into test_det_instrument
		from redcap_data 
		where project_id = new.project_id 
			and event_id = new.event_id 
			and record = new.record 
			and field_name = 'test_det_instrument';
			
		select value into test_det_batchsize
		from redcap_data 
		where project_id = new.project_id 
			and event_id = new.event_id 
			and record = new.record 
			and field_name = 'test_det_batchsize';
		        
		select value into test_det_batch_id
		from redcap_data 
		where project_id = new.project_id 
			and event_id = new.event_id 
			and record = new.record 
			and field_name = 'test_det_batch_id';
		
		-- ------------------------------------------------------------------
		-- Go through the specimen in the batch by looking for the 
		-- barcodes.
		-- ------------------------------------------------------------------	
        s2_loop:repeat
			
            select value
            into c_value
			from redcap_data
			where project_id = new.project_id
				and event_id = new.event_id
				and record = new.record
				and field_name = concat('test_det_barcode_', count_);
			
            -- Update the staging
			update redcap_ddp_specimen
			set TEST_DETECTION_BATCHSIZE = test_det_batchsize,
				TEST_DETECTION_DATETIME = test_det_datetime,
                TEST_DETECTION_INSTRUMENT = test_det_instrument,
                TEST_DETECTION_PERSONNEL = test_det_personnel,
                test_detection_batch_id = test_det_batch_id,
                DETECTION_LAB = detection_lab,
                DETECTION_BATCH_POSITION = count_
			where specimen_barcode = c_value;
            
			set count_ = count_ + 1;
        until count_ > test_det_batchsize
        end repeat;
	
	end if;
end; $$

DELIMITER ;

DROP TRIGGER IF EXISTS `lab_testing_detection_complete_insert_trigger`;

DELIMITER $$
 
create trigger lab_testing_detection_complete_insert_trigger
after insert
on redcap_data for each row
begin

	declare lab_report smallint(4);
	declare lab_receiving smallint(4);
	declare lab_extraction smallint(4);
	declare lab_results smallint(4);
    declare count_ int default 1;
    
	declare report_max_event smallint(4);
	declare detection_lab varchar(255);
	declare test_det_datetime varchar(255);
	declare test_det_personnel varchar(255);
	declare test_det_instrument varchar(255);
	declare test_det_batchsize int;
	declare test_det_batch_id varchar(255);
	declare test_det_barcode varchar(255);
	declare pos varchar(2);
	DECLARE done INT DEFAULT FALSE;
	
	declare c_project_id int(10);
	declare c_event_id int(10);
	declare c_record varchar(100);
	declare c_field_name varchar(100);
	declare c_value text;
	declare c_instance smallint(4);
    
	declare specimen cursor for
	select project_id, event_id, record, field_name, value, instance
	from redcap_data
	where project_id = new.project_id
		and event_id = new.event_id
		and record = new.record
		and field_name like 'test_det_barcode_%';
    
	DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

	select 352 into lab_report;	
	select 362 into lab_receiving;	
	select 344 into lab_extraction;	
	select 345 into lab_results;
	
	-- Stop calling this procedure with any other data
	if (new.field_name = 'testing_detection_complete' and new.value = '2' and new.project_id = lab_results) then
		
		select max(event_id) into report_max_event from redcap_data where project_id = lab_report;
        
        insert into my_log (dt, what) values (now(), '1');
		
		select value into detection_lab
		from redcap_data 
		where project_id = new.project_id 
			and event_id = new.event_id 
			and record = new.record 
			and field_name = 'detection_lab';
            			
		select value into test_det_datetime
		from redcap_data 
		where project_id = new.project_id 
			and event_id = new.event_id 
			and record = new.record 
			and field_name = 'test_det_datetime';
            			
		select value into test_det_personnel
		from redcap_data 
		where project_id = new.project_id 
			and event_id = new.event_id 
			and record = new.record 
			and field_name = 'test_det_personnel';
		
		select value into test_det_instrument
		from redcap_data 
		where project_id = new.project_id 
			and event_id = new.event_id 
			and record = new.record 
			and field_name = 'test_det_instrument';
			
		select value into test_det_batchsize
		from redcap_data 
		where project_id = new.project_id 
			and event_id = new.event_id 
			and record = new.record 
			and field_name = 'test_det_batchsize';
		        
		select value into test_det_batch_id
		from redcap_data 
		where project_id = new.project_id 
			and event_id = new.event_id 
			and record = new.record 
			and field_name = 'test_det_batch_id';
		
		-- ------------------------------------------------------------------
		-- Go through the specimen in the batch by looking for the 
		-- barcodes.
		-- ------------------------------------------------------------------	
        s2_loop:repeat
			
            select value
            into c_value
			from redcap_data
			where project_id = new.project_id
				and event_id = new.event_id
				and record = new.record
				and field_name = concat('test_det_barcode_', count_);
			
            -- Update the staging
			update redcap_ddp_specimen
			set TEST_DETECTION_BATCHSIZE = test_det_batchsize,
				TEST_DETECTION_DATETIME = test_det_datetime,
                TEST_DETECTION_INSTRUMENT = test_det_instrument,
                TEST_DETECTION_PERSONNEL = test_det_personnel,
                DETECTION_LAB = detection_lab,
                test_detection_batch_id = test_det_batch_id,
                DETECTION_BATCH_POSITION = count_
			where specimen_barcode = c_value;
            
			set count_ = count_ + 1;
        until count_ > test_det_batchsize
        end repeat;
	
	end if;
end; $$

DELIMITER ;
