import {Timestamp} from 'rxjs';

export class UserModel {
  public UUID: '';
  public ExtID: number;
  public Login: '';
  public FName = '';
  public MName = '';
  public LName = '';
  public Roles = [];
  public Locked: boolean;
  public TimeLock: number;
  public Email = '';
  public Phone = '';
}
